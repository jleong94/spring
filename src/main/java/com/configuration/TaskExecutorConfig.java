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
        //corePoolSize = (number of CPU cores) * 2 to 4
        executor.setCorePoolSize(10);         // Core threads always alive. Recommended to be based on CPU-bound or I/O-bound nature of tasks.
        //Ask:
        //How many requests/tasks per second?
        //How long does a task take on average?
        //Are tasks synchronous or async?
        //Example:
        //200 tasks/sec
        //Each task takes ~200 ms = 5 tasks per thread/sec
        //You’ll need: Needed threads = 200 / 5 = 40 threads
        executor.setMaxPoolSize(20);          // Max number of concurrent threads allowed when the queue is full.
        executor.setQueueCapacity(500);       // Number of tasks that can wait in the queue before new threads are spawned beyond corePoolSize.
        executor.setThreadNamePrefix("ThreadPoolTaskExecutor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // Backpressure
        executor.initialize();
        return executor;
    }
}
