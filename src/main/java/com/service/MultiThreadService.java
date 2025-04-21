package com.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import com.utilities.Tool;

import lombok.extern.slf4j.Slf4j;

@Service
public class MultiThreadService {
    
    @Autowired
    ApplicationContext applicationContext;
    
    @Autowired
    Tool tool;
	
	public long sampleServiceThread(Logger log, String logFolder, int num_threads) {
    	long result = 0L;
    	ExecutorService executorService;
    	List<Future<?>> futures = new ArrayList<>();
    	MDC.put("mdcId", UUID.randomUUID());
		try {
        	executorService = Executors.newFixedThreadPool(num_threads);
        	Tool tool = applicationContext.getBean(Tool.class);
        	MultiThreadService sampleService = applicationContext.getBean(MultiThreadService.class);
        	for(int i = 0; i < num_threads; i++) {
        		Runnable runnable = new SampleServiceThread(logFolder, i + 1, tool, sampleService);
        		Future<?> future = executorService.submit(runnable);
                futures.add(future);
        	}
        	for (Future<?> future : futures) {
                while (!future.isDone()) {}
            }
        	
        	executorService.shutdown();
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
        return result;
    }
	
	@Slf4j
	static class SampleServiceThread implements Runnable{

		int thread_no;
		Tool tool;
		MultiThreadService sampleService;
		String logFolder;

		SampleServiceThread(String logFolder, int thread_no, Tool tool, MultiThreadService sampleService){
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
}
