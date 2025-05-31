package com.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import com.utilities.Tool;

import lombok.extern.slf4j.Slf4j;

@Service
public class MultiThreadService {
	
	@Qualifier("threadPoolTaskExecutor")
	ThreadPoolTaskExecutor threadPoolTaskExecutor;
    
    @Autowired
    ApplicationContext applicationContext;
    
    @Autowired
    Tool tool;
	
	public void sampleServiceThread(Logger log, String logFolder, int num_threads) {
    	List<Future<?>> futures = new ArrayList<>();
    	List<Future<String>> futures_string = new ArrayList<>();
    	MDC.put("mdcId", UUID.randomUUID());
		try {
        	Tool tool = applicationContext.getBean(Tool.class);
        	MultiThreadService sampleService = applicationContext.getBean(MultiThreadService.class);
        	// Single thread run & let it be
        	threadPoolTaskExecutor.execute(new FireAndForgetTask(logFolder, 1, tool, sampleService));
        	// Multi thread
        	for(int i = 0; i < num_threads; i++) {
        		Future<?> future = threadPoolTaskExecutor.submit(new FireAndForgetTask(logFolder, i + 1, tool, sampleService));
                futures.add(future);
                Future<String> future_string = threadPoolTaskExecutor.submit(new ResultTask(logFolder, i + 1, tool, sampleService));
                futures_string.add(future_string);
        	}
        	for (Future<String> future_string : futures_string) {
        		future_string.get();
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
        } finally {MDC.clear();}
    }
	
	@Slf4j
	static class FireAndForgetTask implements Runnable{

		int thread_no;
		Tool tool;
		MultiThreadService sampleService;
		String logFolder;

		FireAndForgetTask(String logFolder, int thread_no, Tool tool, MultiThreadService sampleService){
			this.thread_no = thread_no;
			this.logFolder = logFolder;
			this.tool = tool;
			this.sampleService = sampleService;
		}

		@Override
		public void run() {
			log.info("START thread process at thread no. " + this.thread_no + "...");
			MDC.put("mdcId", UUID.randomUUID());
			try {
				
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
				log.info("END thread process at thread no. " + this.thread_no + "...");
				MDC.clear();
			}
		}
	}
	
	@Slf4j
	static class ResultTask implements Callable<String>{

		int thread_no;
		Tool tool;
		MultiThreadService sampleService;
		String logFolder;

		ResultTask(String logFolder, int thread_no, Tool tool, MultiThreadService sampleService){
			this.thread_no = thread_no;
			this.logFolder = logFolder;
			this.tool = tool;
			this.sampleService = sampleService;
		}

		@Override
	    public String call() {
			String result = "";
			log.info("START thread process at thread no. " + this.thread_no + "...");
			MDC.put("mdcId", UUID.randomUUID());
			try {
				
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
				log.info("END thread process at thread no. " + this.thread_no + "...");
				MDC.clear();
			}
			return result;
		}
	}
}
