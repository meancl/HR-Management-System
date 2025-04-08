package Hr.Mgr.domain.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

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
        return DataSourceBuilder.create()
                .url("jdbc:mysql://localhost:3306/hr_mgr?useSSL=false&serverTimezone=Asia/Seoul")
                .username("root")
                .password("root")
                .build();
    }
    @Bean(name = "masterReadDataSource")
    public DataSource masterReadDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:mysql://localhost:3306/hr_mgr?useSSL=false&serverTimezone=Asia/Seoul")
                .username("root")
                .password("root")
                .build();
    }
    @Bean(name = "replicaWriteDataSource")
    public DataSource replicaWriteDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:mysql://mjhr.duckdns.org:3306/hr_mgr?useSSL=false&serverTimezone=Asia/Seoul")
                .username("repl")
                .password("repl")
                .build();
    }



    @Bean(name = "replicaReadDataSource")
    public DataSource replicaReadDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:mysql://localhost:3306/hr_mgr?useSSL=false&serverTimezone=Asia/Seoul")
                .username("root")
                .password("root")
                .build();
    }

    @Bean(name = "defaultDataSource")
    public DataSource defaultDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:mysql://mjhr.duckdns.org:3306/hr_mgr?useSSL=false&serverTimezone=Asia/Seoul")
                .username("root")
                .password("root")
                .build();
    }


}

