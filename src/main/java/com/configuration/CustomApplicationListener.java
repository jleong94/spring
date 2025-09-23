package com.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.repo.JdbcRepo;

@Slf4j
@Component
public class CustomApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

	private final JdbcRepo jdbcRepo;

	public CustomApplicationListener(JdbcRepo jdbcRepo) {
		this.jdbcRepo = jdbcRepo;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		try {
			//Automate run script located at classpath once JPA done execution
			Resource[] scripts = new PathMatchingResourcePatternResolver()
					.getResources("classpath*:db_script/*.sql");
			for (Resource script : scripts) {
				jdbcRepo.runScriptFile(log, script);
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
