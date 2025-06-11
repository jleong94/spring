package com.configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class TaskExecutorConfig {

	@Bean(name = "threadPoolTaskExecutor")
    Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);         // Core threads always alive
        executor.setMaxPoolSize(20);          // Max threads allowed
        executor.setQueueCapacity(500);       // Tasks that wait in queue
        executor.setThreadNamePrefix("ThreadPoolTaskExecutor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // Backpressure
        executor.initialize();
        return executor;
    }
}
