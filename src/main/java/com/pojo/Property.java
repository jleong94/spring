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
	@Value("${smtp.mail}")
    private String smtp_mail;
	
	@Value("${smtp.host}")
    private String smtp_host;
	
	@Value("${smtp.port}")
    private int smtpPort;
	
	@Value("${smtp.username}")
    private String smtp_username;
	
	@Value("${smtp.password}")
    private String smtp_password;
	
	@Value("${smtp.tls}")
    private boolean smtp_tls;
	
	@Value("${smtp.ssl}")
    private boolean smtp_ssl;
	
	@Value("${mail.upload.path}")
    private String mail_upload_path;
	
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
