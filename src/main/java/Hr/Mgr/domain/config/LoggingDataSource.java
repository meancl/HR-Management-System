package Hr.Mgr.domain.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class LoggingDataSource extends AbstractDataSource {

    private final DataSource delegate;
    private final String name;

    public LoggingDataSource(DataSource delegate, String name) {
        this.delegate = delegate;
        this.name = name;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = delegate.getConnection();
//        System.out.println("✅ [Connection] getConnection from [" + name + "]: " + conn);
        return conn;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection conn = delegate.getConnection(username, password);
//        System.out.println("✅ [Connection] getConnection from [" + name + "]: " + conn);
        return conn;
    }
}
