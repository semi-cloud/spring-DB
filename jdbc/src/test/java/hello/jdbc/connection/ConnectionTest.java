package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    @Test
    void driverManager() throws SQLException {
        Connection connection1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection connection2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection1={} {}", connection1, connection1.getClass());
        log.info("connection2={} {}", connection2, connection2.getClass());
    }

    @Test
    void dataSourceDriverManager() throws SQLException {
        // DriverManagerDataSource : 항상 새로운 커넥션 생성, 부모는 DataSource
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        useDataSource(dataSource);
    }

    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        // 커넥션 풀링
        HikariDataSource dataSource = new HikariDataSource();   // DataSource 구현
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("MyPool");

        useDataSource(dataSource);
        Thread.sleep(1000);    // 별도의 스레드에서 커넥션을 10개 생성하는 작업이 이루어지기 때문에 직접 보려면 지연 시간 추가
    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection connection1 = dataSource.getConnection();   // 인터페이스를 통해 가져옴
        Connection connection2 = dataSource.getConnection();
        log.info("connection1={} {}", connection1, connection1.getClass());
        log.info("connection2={} {}", connection2, connection2.getClass());
    }
}
