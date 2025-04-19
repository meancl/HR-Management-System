package Hr.Mgr.domain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class Config {
    @Value("${attendance-calculate-threads}")
    private int maxThreads;
    @Bean
    public ExecutorService sharedExecutorService() {
        return Executors.newFixedThreadPool(maxThreads);  // 전체 합쳐서 8개까지만
    }
}