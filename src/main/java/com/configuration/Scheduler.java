package com.configuration;

import java.util.Deque;
import java.util.UUID;

import javax.cache.Cache;
import javax.cache.CacheManager;

import org.jboss.logging.MDC;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.retry.annotation.Backoff;

import com.pojo.template.Pojo;
import com.service.template.SampleThreadService;
import com.utilities.Tool;

import lombok.extern.slf4j.Slf4j;

/*
 * Here will configure the trigger date time & it's parameter for each task created in class, BatchJobConfig
 * */
@Slf4j
@Configuration
@EnableScheduling//To allow scheduled tasks
@ConditionalOnProperty(//Only create this bean if a specific property has a specific value.
		prefix = "spring.task.scheduling",
		name = "enabled",
		havingValue = "true",
		matchIfMissing = false
		)
public class Scheduler {
	
	@Autowired
	Tool tool;
	
	@Autowired
	CacheManager cacheManager;
	
	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	@Qualifier("sampleJob")//To match with bean name for created job in BatchJobConfig
	Job job;
	
	@Retryable(//Retry the method on exception
			retryFor = { Throwable.class },
            maxAttempts = 5,//Retry up to nth times
            /*
             * backoff = Delay before each retry
             * delay = Start with nth seconds
             * multiplier = Exponential backoff (2s, 4s, 8s...)
             * */
            backoff = @Backoff(delay = 1000, multiplier = 2)
        )
	@Scheduled(fixedRate = 300_000, zone = "Asia/Kuala_Lumpur")
	@Async//Run on separate thread, non-blocking the scheduler
	public void clearBucketsCache() throws Throwable {
        MDC.put("X-Request-ID", UUID.randomUUID());
		try {
			Cache<String, ?> cache = cacheManager.getCache("buckets");
			if (cache != null) {
	            cache.removeAll();
	            log.info("Cleared 'buckets' cache globally!");
	        }
        } catch(Throwable e) {
        	// Get the current stack trace element
			StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
			// Find matching stack trace element from exception
			for (StackTraceElement element : e.getStackTrace()) {
				if (currentElement.getClassName().equals(element.getClassName())
						&& currentElement.getMethodName().equals(element.getMethodName())) {
					log.error("Error in {} at line {}: {} - {}",
							element.getClassName(),
							element.getLineNumber(),
							e.getClass().getName(),
							e.getMessage());
					break;
				}
			}
			throw e;
        } finally{
        	MDC.clear();
        }
    } 

	@Retryable(//Retry the method on exception
			retryFor = { Throwable.class },
            maxAttempts = 5,//Retry up to nth times
            /*
             * backoff = Delay before each retry
             * delay = Start with nth seconds
             * multiplier = Exponential backoff (2s, 4s, 8s...)
             * */
            backoff = @Backoff(delay = 1000, multiplier = 2)
        )
	@Scheduled(cron = "*/5 * * * * *", zone = "Asia/Kuala_Lumpur")
	@Async//Run on separate thread, non-blocking the scheduler
	public void sampleTask() throws Throwable {
        MDC.put("X-Request-ID", UUID.randomUUID());
		try {
        	JobParameters parameters = new JobParametersBuilder()
        			.toJobParameters();
        	jobLauncher.run(job, parameters);
        } catch(Throwable e) {
        	// Get the current stack trace element
			StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
			// Find matching stack trace element from exception
			for (StackTraceElement element : e.getStackTrace()) {
				if (currentElement.getClassName().equals(element.getClassName())
						&& currentElement.getMethodName().equals(element.getMethodName())) {
					log.error("Error in {} at line {}: {} - {}",
							element.getClassName(),
							element.getLineNumber(),
							e.getClass().getName(),
							e.getMessage());
					break;
				}
			}
			throw e;
        } finally{
        	MDC.clear();
        }
    }

    @Autowired
    private SampleThreadService sampleThreadService;
	
	@Scheduled(cron = "*/10 * * * * *", zone = "Asia/Kuala_Lumpur")
	@Async//Run on separate thread, non-blocking the scheduler
	public void sampleTask2() throws Throwable {
		UUID xRequestId = UUID.randomUUID();
        MDC.put("X-Request-ID", xRequestId);
        log.info("Sample task 2 start.");
		try {
			Deque<Pojo> deque = sampleThreadService.addDummyRecord(1000);
			for(int i = 0; i < 2; i++) {
				sampleThreadService.processRecords(xRequestId, (i + 1), deque);
			}
        } catch(Throwable e) {
        	// Get the current stack trace element
			StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
			// Find matching stack trace element from exception
			for (StackTraceElement element : e.getStackTrace()) {
				if (currentElement.getClassName().equals(element.getClassName())
						&& currentElement.getMethodName().equals(element.getMethodName())) {
					log.error("Error in {} at line {}: {} - {}",
							element.getClassName(),
							element.getLineNumber(),
							e.getClass().getName(),
							e.getMessage());
					break;
				}
			}
			throw e;
        } finally{
            log.info("Sample task 2 end.");
        	MDC.clear();
        }
    }
}
