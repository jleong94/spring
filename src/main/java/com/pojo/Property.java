package com.pojo;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

/*
 * Here is to load all the parameter & it's value created in all properties file located in classpath properties folder
 * */
@Getter//Auto generate getter method
@Configuration
public class Property {

	//Rate limit
	@Value("${rate.limit.capacity}")
    private int rate_limit_capacity;

	@Value("${rate.limit.tokens}")
    private int rate_limit_tokens;
	
	@Value("${rate.limit.period}")
    private int rate_limit_period;

	//Email SMTP
	@Value("${spring.mail.username}")
    private String spring_mail_username;
	
	//CORS
	@Value("#{'${allowed.origins}'.split(',')}")
    private List<String> allowed_origins;
	
	//Keycloak
	@Value("${keycloak.base_url}")
    private String keycloak_base_url;
	
	@Value("${keycloak.realm}")
    private String keycloak_realm;
	
	@Value("${keycloak.client-id}")
    private String keycloak_client_id;
	
	@Value("${keycloak.username}")
    private String keycloak_username;
	
	@Value("${keycloak.password}")
    private String keycloak_password;
}
