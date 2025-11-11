package com.configuration;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Comparator;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.repo.JdbcRepo;

@Slf4j
@Component
public class StartupScriptRunner {

	private final JdbcRepo jdbcRepo;

	public StartupScriptRunner(JdbcRepo jdbcRepo) {
		this.jdbcRepo = jdbcRepo;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void executeStartupScript() {
		int successCount = 0, failCount = 0;
		try {
			//Automate run script located at classpath once JPA done execution
			Resource[] scripts = new PathMatchingResourcePatternResolver()
					.getResources("classpath*:db_script/*.sql");
			// Sort scripts by filename to ensure consistent execution order
			// Recommendation: Name scripts like 01_init.sql, 02_seed.sql, etc.
			Arrays.sort(scripts, Comparator.comparing(Resource::getFilename));
			log.info("Found {} SQL script(s) to execute", scripts.length);
			for (Resource script : scripts) {
				try {
					log.info("Executing script: {}", script.getFilename());
					jdbcRepo.runScriptFile(log, script);
					successCount++;
				} catch (Throwable e) {
					failCount++;
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
				}
			}
            
            log.info("Startup script execution completed. Success: {}, Failed: {}", successCount, failCount);
            
            if (failCount > 0) {
                log.warn("Some startup scripts failed. Please check the logs above.");
            }
		} catch (Throwable e) {
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
		}
	}
}
