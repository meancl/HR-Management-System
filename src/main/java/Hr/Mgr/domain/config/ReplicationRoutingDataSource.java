package Hr.Mgr.domain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {
    @Value("${server-name}")
    String serverName;

    @Override
    protected Object determineCurrentLookupKey() {
        String key = DbContextHolder.getCurrentDb(); // "master" or "replica"
        if( key == null ) return serverName.equalsIgnoreCase("master") ? "master-write" : "replica-write";
        return key;

    }
}