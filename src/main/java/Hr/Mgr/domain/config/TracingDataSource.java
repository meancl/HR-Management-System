package Hr.Mgr.domain.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TracingDataSource extends AbstractDataSource {
    private final DataSource delegate;
    private final String name;
    private static final Logger logger = LoggerFactory.getLogger(AbstractDataSource.class);
    public TracingDataSource(DataSource delegate, String name) {
        this.delegate = delegate;
        this.name = name;
    }
    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = delegate.getConnection();
        logger.info("[Connection] getConnection from [" + name + "]: " + conn);
        return conn;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection conn = delegate.getConnection(username, password);
        logger.info("[Connection] getConnection from [" + name + "]: " + conn);
        return conn;
    }
}
