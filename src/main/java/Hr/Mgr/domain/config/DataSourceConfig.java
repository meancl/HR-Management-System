package Hr.Mgr.domain.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import com.zaxxer.hikari.HikariConfig;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Primary
    @Bean(name = "dataSource") // 최종적으로 Spring이 사용할 DataSource
    public DataSource dataSource(
            @Qualifier("routingDataSource") DataSource routingDataSource
    ) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    @Bean(name = "routingDataSource")
    public DataSource routingDataSource(
            @Qualifier("masterWriteDataSource") DataSource masterWriteDataSource,
            @Qualifier("masterReadDataSource") DataSource masterReadDataSource,
            @Qualifier("replicaWriteDataSource") DataSource replicaWriteDataSource,
            @Qualifier("replicaReadDataSource") DataSource replicaReadDataSource,
            @Qualifier("defaultDataSource") DataSource defaultDataSource
    ) {

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("master-write", masterWriteDataSource);
        targetDataSources.put("master-read", masterReadDataSource);
        targetDataSources.put("replica-write", replicaWriteDataSource);
        targetDataSources.put("replica-read", replicaReadDataSource);

        ReplicationRoutingDataSource routingDataSource = new ReplicationRoutingDataSource();
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(defaultDataSource);

        return routingDataSource;
    }

    @Bean(name = "masterWriteDataSource")
    public DataSource masterWriteDataSource() {
        return createDataSource("jdbc:mysql://localhost:3306/hr_mgr", "masterWriteDataSource");
    }

    @Bean(name = "masterReadDataSource")
    public DataSource masterReadDataSource() {
        return createDataSource("jdbc:mysql://localhost:3306/hr_mgr", "masterReadDataSource");
    }

    @Bean(name = "replicaWriteDataSource")
    public DataSource replicaWriteDataSource() {
        return createDataSource("jdbc:mysql://mjhr.duckdns.org:3306/hr_mgr", "replicaWriteDataSource");
    }

    @Bean(name = "replicaReadDataSource")
    public DataSource replicaReadDataSource() {
        return createDataSource("jdbc:mysql://localhost:3306/hr_mgr", "replicaReadDataSource");
    }

    @Bean(name = "defaultDataSource")
    public DataSource defaultDataSource() {
        return createDataSource("jdbc:mysql://mjhr.duckdns.org:3306/hr_mgr", "defaultDataSource");
    }

    private DataSource createDataSource(String jdbcUrl, String name) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername("root");
        config.setPassword("root");
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);

        HikariDataSource hikari =  new HikariDataSource(config);
        return new TracingDataSource(hikari, name);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(@Qualifier("dataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

}

