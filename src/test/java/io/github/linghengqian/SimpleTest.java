package io.github.linghengqian;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import javax.sql.DataSource;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings({"resource", "SqlNoDataSourceInspection"})
@Testcontainers
public class SimpleTest {

    private static final Network NETWORK = Network.newNetwork();

    @Container
    private static final GenericContainer<?> CLICKHOUSE_KEEPER_CONTAINER = new GenericContainer<>("clickhouse/clickhouse-keeper:24.11.1.2557")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Paths.get("src/test/resources/keeper_config.xml").toAbsolutePath()),
                    "/etc/clickhouse-keeper/keeper_config.xml"
            )
            .withNetwork(NETWORK)
            .withNetworkAliases("clickhouse-keeper-01");

    @Container
    public static final GenericContainer<?> CONTAINER = new GenericContainer<>("clickhouse/clickhouse-server:24.11.1.2557")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Paths.get("src/test/resources/transactions.xml").toAbsolutePath()),
                    "/etc/clickhouse-server/config.d/transactions.xml"
            )
            .withNetwork(NETWORK)
            .withExposedPorts(8123)
            .dependsOn(CLICKHOUSE_KEEPER_CONTAINER);

    private String jdbcUrlPrefix;

    @AfterAll
    static void afterAll() {
        NETWORK.close();
    }

    @Test
    void test() throws SQLException {
        jdbcUrlPrefix = "jdbc:ch://localhost:" + CONTAINER.getMappedPort(8123) + "/";
        // todo wait clickhouse keeper
        await().pollDelay(Duration.ofSeconds(5L)).until(() -> true);
        try (Connection connection = openConnection("default");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE demo_ds");
        }
        try (Connection connection = openConnection("demo_ds");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    create table IF NOT EXISTS t_order (
                        order_id   Int64 NOT NULL DEFAULT rand(),
                        order_type Int32,
                        user_id    Int32 NOT NULL,
                        address_id Int64 NOT NULL,
                        status     VARCHAR(50)
                    ) engine = MergeTree
                          primary key (order_id)
                          order by (order_id)""");
            statement.executeUpdate("TRUNCATE TABLE t_order");
        }
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
        config.setJdbcUrl(jdbcUrlPrefix + "demo_ds?transactionSupport=true");
        DataSource dataSource = new HikariDataSource(config);
        IntStream.range(1, 11).parallel().forEach(i -> {
            Order order = new Order(0, i % 2, i, i, "INSERT_TEST");
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (?, ?, ?, ?)",
                         Statement.NO_GENERATED_KEYS)) {
                statement.setInt(1, order.userId());
                statement.setInt(2, order.orderType());
                statement.setLong(3, order.addressId());
                statement.setString(4, order.status());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        IntStream.range(1, 11).parallel().forEach(i -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("alter table t_order delete where address_id=?")) {
                statement.setLong(1, i);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Connection openConnection(final String databaseName) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", "default");
        props.setProperty("password", "");
        return DriverManager.getConnection(jdbcUrlPrefix + databaseName + "?transactionSupport=true", props);
    }

    public List<Order> selectAll(DataSource dataSource) throws SQLException {
        List<Order> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM t_order");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                result.add(
                        new Order(
                                resultSet.getLong(1),
                                resultSet.getInt(2),
                                resultSet.getInt(3),
                                resultSet.getLong(4),
                                resultSet.getString(5)
                        ));
            }
        }
        return result;
    }
}
