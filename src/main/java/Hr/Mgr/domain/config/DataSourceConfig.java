package Hr.Mgr.domain.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import com.zaxxer.hikari.HikariConfig;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Primary
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
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/hr_mgr?useSSL=false&serverTimezone=Asia/Seoul");
        config.setUsername("root");
        config.setPassword("root");
        config.setMaximumPoolSize(20);;
        config.setMinimumIdle(5);
        return new HikariDataSource(config);
    }
    @Bean(name = "masterReadDataSource")
    public DataSource masterReadDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/hr_mgr?useSSL=false&serverTimezone=Asia/Seoul");
        config.setUsername("root");
        config.setPassword("root");
        config.setMaximumPoolSize(20);;
        config.setMinimumIdle(5);
        return new HikariDataSource(config);
    }
    @Bean(name = "replicaWriteDataSource")
    public DataSource replicaWriteDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://mjhr.duckdns.org:3306/hr_mgr?useSSL=false&serverTimezone=Asia/Seoul");
        config.setUsername("root");
        config.setPassword("root");
        config.setMaximumPoolSize(20);;
        config.setMinimumIdle(5);
        return new HikariDataSource(config);
    }



    @Bean(name = "replicaReadDataSource")
    public DataSource replicaReadDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/hr_mgr?useSSL=false&serverTimezone=Asia/Seoul");
        config.setUsername("root");
        config.setPassword("root");
        config.setMaximumPoolSize(20);;
        config.setMinimumIdle(5);
        return new HikariDataSource(config);
    }

    @Bean(name = "defaultDataSource")
    public DataSource defaultDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://mjhr.duckdns.org:3306/hr_mgr?useSSL=false&serverTimezone=Asia/Seoul");
        config.setUsername("root");
        config.setPassword("root");
        config.setMaximumPoolSize(20);;
        config.setMinimumIdle(5);
        return new HikariDataSource(config);
    }


}

