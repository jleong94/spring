package com.configuration;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Slf4j
@Component
public class CustomApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

	private final DataSource dataSource;

	public CustomApplicationListener(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		try {
			//Automate run script located at classpath once JPA done execution
			Resource[] scripts = new PathMatchingResourcePatternResolver()
					.getResources("classpath:db_script/*.sql");
			for (Resource script : scripts) {
				log.info("Running SQL script: {}", script.getFilename());
				@Cleanup Connection connDB = dataSource.getConnection();
				ScriptUtils.executeSqlScript(connDB, new EncodedResource(script), true, true, "--", ";", "/*", "*/");
				log.info("SQL script executed successfully.");
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
