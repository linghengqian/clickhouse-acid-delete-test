package io.github.linghengqian;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Duration;
import java.util.Properties;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.await;

@SuppressWarnings({"resource", "SqlNoDataSourceInspection"})
@Testcontainers
public class MySqlTest {

    @Container
    public static final GenericContainer<?> CONTAINER = new GenericContainer<>("mysql:9.1.0-oraclelinux9")
            .withEnv("MYSQL_ROOT_PASSWORD", "example")
            .withExposedPorts(3306);

    private String jdbcUrlPrefix;

    @Test
    void testMySql() throws SQLException {
        jdbcUrlPrefix = "jdbc:mysql://localhost:" + CONTAINER.getMappedPort(3306) + "/";
        await().atMost(Duration.ofMinutes(1L)).ignoreExceptions().until(() -> {
            openConnection("").close();
            return true;
        });
        try (Connection connection = openConnection("");
             Statement st = connection.createStatement()) {
            st.executeUpdate("CREATE DATABASE demo_ds");
        }
        try (Connection connection = openConnection("demo_ds");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS t_order
                    (order_id BIGINT NOT NULL AUTO_INCREMENT,
                    order_type INT(11),
                    user_id INT NOT NULL,
                    address_id BIGINT NOT NULL,
                    status VARCHAR(50),
                    PRIMARY KEY (order_id))""");
            statement.executeUpdate("TRUNCATE TABLE t_order");
        }
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl(jdbcUrlPrefix + "demo_ds");
        config.setUsername("root");
        config.setPassword("example");
        DataSource dataSource = new HikariDataSource(config);
        IntStream.range(1, 11).parallel().forEach(i -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, i);
                ps.setInt(2, i % 2);
                ps.setLong(3, i);
                ps.setString(4, "INSERT_TEST");
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        IntStream.range(1, 11).parallel().forEach(i -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM t_order WHERE address_id=?")) {
                ps.setLong(1, i);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        dataSource.unwrap(HikariDataSource.class).close();
    }

    private Connection openConnection(final String databaseName) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", "root");
        props.setProperty("password", "example");
        return DriverManager.getConnection(jdbcUrlPrefix + databaseName, props);
    }
}
