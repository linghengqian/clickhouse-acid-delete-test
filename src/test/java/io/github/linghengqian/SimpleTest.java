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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Properties;

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
        await().atMost(Duration.ofMinutes(1L)).ignoreExceptions().until(() -> {
            openConnection("default").close();
            return true;
        });
        try (Connection connection = openConnection("default");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE demo_ds");
        }
        initTable();
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
        config.setJdbcUrl(jdbcUrlPrefix + "demo_ds?transactionSupport=true");
        DataSource dataSource = new HikariDataSource(config);
    }

    private Connection openConnection(final String databaseName) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", "default");
        props.setProperty("password", "");
        return DriverManager.getConnection(jdbcUrlPrefix + databaseName, props);
    }

    private void initTable() {
        try (Connection connection = openConnection("demo_ds");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    create table IF NOT EXISTS t_order
                    (
                        order_id   Int64 NOT NULL,
                        order_type Int32,
                        user_id    Int32 NOT NULL,
                        address_id Int64 NOT NULL,
                        status     VARCHAR(50)
                    ) engine = MergeTree
                          primary key (order_id)
                          order by (order_id)""");
            statement.executeUpdate("TRUNCATE TABLE t_order");
        } catch (final SQLException exception) {
            throw new RuntimeException(exception);
        }
    }
}
