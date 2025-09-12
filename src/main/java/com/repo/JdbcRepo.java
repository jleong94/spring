package com.repo;

import java.sql.Connection;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Repository;

import lombok.Cleanup;

@Repository
public class JdbcRepo {

	private final DataSource dataSource;

    public JdbcRepo(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public boolean runScriptFile(Logger log, Resource script) throws Throwable {
    	boolean result = false;
    	try {
    		log.info("Running SQL script: {}", script.getFilename());
			@Cleanup Connection connDB = dataSource.getConnection();
			ScriptUtils.executeSqlScript(connDB, new EncodedResource(script), true, true, "--", ";", "/*", "*/");
			log.info("SQL script executed successfully.");
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
		} finally {
			
		}
    	return result;
    }
    
    /*
    public void jdbcTemplate(Logger log) throws Throwable {		
		int count = 1;
    	try {
    		String sql = new StringBuffer()
    				.toString();
    		@Cleanup Connection connDB = dataSource.getConnection();
    		@Cleanup PreparedStatement ps = connDB.prepareStatement(sql);
    		//Set filter param on prepared statement
    		
    		@Cleanup ResultSet rs = ps.executeQuery();
    		
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
		} finally {
			
		}
    }
    */
}
