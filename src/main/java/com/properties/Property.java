package com.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import lombok.Getter;

/*
 * Here is to load all the parameter & it's value created in all properties file located in classpath properties folder
 * */
@Getter//Auto generate getter method
@Configuration
@PropertySources({@PropertySource("classpath:/properties/system.properties"),
	@PropertySource("classpath:/properties/jwt.properties"),
	@PropertySource("classpath:/properties/mail.properties")})
public class Property {
	
	@Value("${app.name}")
    private String app_name;

	@Value("${project.log.name}")
    private String project_log_name;

	@Value("${rate.limit.requests}")
    private int rate_limit_requests;

	@Value("${rate.limit.duration}")
    private int rate_limit_duration;

	@Value("${rate.limit.unit}")
    private String rate_limit_unit;

	@Value("${jwt.secret.key}")
    private String jwt_secret_key;

	@Value("${jwt.token.expiration}")
    private int jwt_token_expiration;
	
	@Value("${rate.limit.reset}")
    private int rate_limit_reset;
	
	@Value("${jwt.token.type}")
    private String jwt_token_type;
	
	@Value("${smtp.host}")
    private String smtpHost;
	
	@Value("${smtp.port}")
    private int smtpPort;
	
	@Value("${smtp.username}")
    private String smtpUsername;
	
	@Value("${smtp.password}")
    private String smtpPassword;
	
	@Value("${smtp.tls}")
    private boolean smtpTLS;
	
	@Value("${smtp.ssl}")
    private boolean smtpSSL;
}
