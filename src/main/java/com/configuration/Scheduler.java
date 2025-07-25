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

import com.pojo.template.SampleSharedStack;
import com.service.template.SampleThreadService;
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
	@Scheduled(cron = "*/5 * * * * *", zone = "Asia/Kuala_Lumpur")
	@Async//Run on separate thread, non-blocking the scheduler 
	public void sampleTask() {
        MDC.put("mdcId", UUID.randomUUID());
		try {
        	JobParameters parameters = new JobParametersBuilder()
        			.toJobParameters();
        	jobLauncher.run(job, parameters);
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
	
	@Autowired
    private SampleSharedStack sampleSharedStack;

    @Autowired
    private SampleThreadService SampleThreadService;
	
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
	@Scheduled(cron = "*/10 * * * * *", zone = "Asia/Kuala_Lumpur")
	@Async//Run on separate thread, non-blocking the scheduler  
	public void sampleTask2() {
        MDC.put("mdcId", UUID.randomUUID());
        log.info("Sample task 2 start.");
		try {
			sampleSharedStack.addDummyRecord(1000);
			for(int i = 0; i < 2; i++) {
				SampleThreadService.processRecords(log, ("Process dummy records at thread " + (i + 1)));
			}
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
            log.info("Sample task 2 end.");
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
