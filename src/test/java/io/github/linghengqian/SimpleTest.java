package io.github.linghengqian;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
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
import java.util.Properties;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.await;

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
            .withExposedPorts(9181)
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
        String connectionString = CLICKHOUSE_KEEPER_CONTAINER.getHost() + ":" + CLICKHOUSE_KEEPER_CONTAINER.getMappedPort(9181);
        await().atMost(Duration.ofMinutes(1L)).ignoreExceptions().until(() -> {
            try (CuratorFramework client = CuratorFrameworkFactory.builder()
                    .connectString(connectionString)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                    .build()) {
                client.start();
            }
            openConnection("default").close();
            return true;
        });
        try (Connection connection = openConnection("default");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE demo_ds");
        }
        try (Connection connection = openConnection("demo_ds");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    create table IF NOT EXISTS t_order (
                        order_id   Int64 NOT NULL,
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
            Order order = new Order(0L, i % 2, i, i, "INSERT_TEST");
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (?, ?, ?, ?)",
                         Statement.NO_GENERATED_KEYS)) {
                ps.setInt(1, order.userId());
                ps.setInt(2, order.orderType());
                ps.setLong(3, order.addressId());
                ps.setString(4, order.status());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        IntStream.range(1, 11).parallel().forEach(i -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("alter table t_order delete where address_id=?")) {
                ps.setLong(1, i);
                ps.executeUpdate();
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
}
