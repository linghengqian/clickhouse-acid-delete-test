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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
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

    private DataSource dataSource;

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
        dataSource = new HikariDataSource(config);
        IntStream.range(1, 11).forEachOrdered(i -> {
            Order order = new Order();
            order.setUserId(i);
            order.setOrderType(i % 2);
            order.setAddressId(i);
            order.setStatus("INSERT_TEST");
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (?, ?, ?, ?)",
                         Statement.NO_GENERATED_KEYS)) {
                statement.setInt(1, order.getUserId());
                statement.setInt(2, order.getOrderType());
                statement.setLong(3, order.getAddressId());
                statement.setString(4, order.getStatus());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        Collection<Order> orders = selectAll();
        List<Integer> orderTypeList = orders.stream().map(Order::getOrderType).toList();
        assertThat(orderTypeList.size(), equalTo(10));
        assertThat(new HashSet<>(orderTypeList), equalTo(Set.of(0, 1)));
        assertThat(orders.stream().map(Order::getUserId).collect(Collectors.toSet()),
                equalTo(Stream.of(2, 4, 6, 8, 10, 1, 3, 5, 7, 9).collect(Collectors.toSet())));
        assertThat(orders.stream().map(Order::getAddressId).collect(Collectors.toSet()),
                equalTo(Stream.of(2L, 4L, 6L, 8L, 10L, 1L, 3L, 5L, 7L, 9L).collect(Collectors.toSet())));
        assertThat(orders.stream().map(Order::getStatus).collect(Collectors.toList()),
                equalTo(IntStream.range(1, 11).mapToObj(_ -> "INSERT_TEST").collect(Collectors.toList())));
        IntStream.range(1, 11).forEachOrdered(i -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("alter table t_order delete where address_id=?")) {
                statement.setLong(1, i);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        assertThat(selectAll(), equalTo(Collections.emptyList()));
    }

    private Connection openConnection(final String databaseName) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", "default");
        props.setProperty("password", "");
        return DriverManager.getConnection(jdbcUrlPrefix + databaseName + "?transactionSupport=true", props);
    }

    public List<Order> selectAll() throws SQLException {
        List<Order> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM t_order");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Order order = new Order();
                order.setOrderId(resultSet.getLong(1));
                order.setOrderType(resultSet.getInt(2));
                order.setUserId(resultSet.getInt(3));
                order.setAddressId(resultSet.getLong(4));
                order.setStatus(resultSet.getString(5));
                result.add(order);
            }
        }
        return result;
    }
}
