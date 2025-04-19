package com.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/*
 * Here is to load all the parameter & it's value created in all properties file located in classpath properties folder
 * */
@Configuration
@PropertySources({@PropertySource("classpath:/properties/system.properties"),
	@PropertySource("classpath:/properties/jwt.properties")})
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

	public String getApp_name() {
		return app_name;
	}

	public void setApp_name(String app_name) {
		this.app_name = app_name;
	}

	public String getProject_log_name() {
		return project_log_name;
	}

	public void setProject_log_name(String project_log_name) {
		this.project_log_name = project_log_name;
	}

	public int getRate_limit_requests() {
		return rate_limit_requests;
	}

	public void setRate_limit_requests(int rate_limit_requests) {
		this.rate_limit_requests = rate_limit_requests;
	}

	public int getRate_limit_duration() {
		return rate_limit_duration;
	}

	public void setRate_limit_duration(int rate_limit_duration) {
		this.rate_limit_duration = rate_limit_duration;
	}

	public String getRate_limit_unit() {
		return rate_limit_unit;
	}

	public void setRate_limit_unit(String rate_limit_unit) {
		this.rate_limit_unit = rate_limit_unit;
	}

	public String getJwt_secret_key() {
		return jwt_secret_key;
	}

	public void setJwt_secret_key(String jwt_secret_key) {
		this.jwt_secret_key = jwt_secret_key;
	}

	public int getJwt_token_expiration() {
		return jwt_token_expiration;
	}

	public void setJwt_token_expiration(int jwt_token_expiration) {
		this.jwt_token_expiration = jwt_token_expiration;
	}

	public int getRate_limit_reset() {
		return rate_limit_reset;
	}

	public void setRate_limit_reset(int rate_limit_reset) {
		this.rate_limit_reset = rate_limit_reset;
	}

	public String getJwt_token_type() {
		return jwt_token_type;
	}	

	public void setJwt_token_type(String jwt_token_type) {
		this.jwt_token_type = jwt_token_type;
	}
	
}
