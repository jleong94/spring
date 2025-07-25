package com.configuration;

import java.util.UUID;

import org.jboss.logging.MDC;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.retry.annotation.Backoff;

import com.utilities.Tool;

import lombok.extern.slf4j.Slf4j;

/*
 * Here will configure the trigger date time & it's parameter for each task created in class, BatchConfig
 * */
@Slf4j
@Configuration
@EnableScheduling//To allow scheduled tasks
public class Scheduler {
	
	@Autowired
	Tool tool;
	
	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	@Qualifier("sampleJob")//To match with bean name for created job in BatchConfig
	Job job;

	@Retryable(//Retry the method on exception
            value = { Exception.class },
            maxAttempts = 5,//Retry up to nth times
            /*
             * backoff = Delay before each retry
             * delay = Start with nth seconds
             * multiplier = Exponential backoff (2s, 4s, 8s...)
             * */
            backoff = @Backoff(delay = 1000, multiplier = 2)
        )
	@Scheduled(fixedRate = (1 * 60 * 60 * 1000))//Run every 5 seconds
	@Async//Run on separate thread, non-blocking the scheduler 
	public void sampleTask() {
        MDC.put("mdcId", UUID.randomUUID());
		try {
        	JobParameters parameters = new JobParametersBuilder()
        			.toJobParameters();
        	jobLauncher.run(job, parameters);
        	/*for(int i = 0; i < 20; i ++) {
        		log.info("sampleTask message " + i);
        		if(i == 9) {
        			Thread.sleep(10 * 1000);
        		}
        	}*/
        } catch(Exception e) {
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
        } finally{
        	MDC.clear();
        }
    }
	
	@Retryable(//Retry the method on exception
            value = { Exception.class },
            maxAttempts = 5,//Retry up to nth times
            /*
             * backoff = Delay before each retry
             * delay = Start with nth seconds
             * multiplier = Exponential backoff (2s, 4s, 8s...)
             * */
            backoff = @Backoff(delay = 1000, multiplier = 2)
        )
	@Scheduled(cron = "*/3600 * * * * *", zone = "Asia/Kuala_Lumpur")//cron expression to perform scheduling
	@Async//Run on separate thread, non-blocking the scheduler  
	public void sampleTask2() {
        MDC.put("mdcId", UUID.randomUUID());
		try {
        	/*for(int i = 0; i < 20; i ++) {
        		log.info("sampleTask2 message " + i);
        	}*/
        } catch(Exception e) {
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
        } finally{
        	MDC.clear();
        }
    }
	
	@Recover //Fallback when all attempts fail
    public void recover(RuntimeException e, UUID uuid) {
		MDC.put("mdcId", uuid); // Restore MDC manually
        log.error("Recovering from task failure: " + e.getMessage());
        MDC.clear();
    }
}
