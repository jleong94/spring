package com.pojo;

import java.util.List;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
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
	@Value("${spring.mail.host}")
    private String spring_mail_host;
	
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
	
	@Bean
    Keycloak keycloakAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloak_base_url)
                .realm(keycloak_realm) // This is the authentication realm (where the admin user exists)
                .clientId(keycloak_client_id)
                .username(keycloak_username)
                .password(keycloak_password)
                .grantType(OAuth2Constants.PASSWORD)
                .build();
    }
	
	//Server
	@Value("${server.ssl.key-store}")
    private String server_ssl_key_store;

	@Value("${server.ssl.key-store-password}")
    private String server_ssl_key_store_password;

	@Value("${server.ssl.trust-store}")
    private String server_ssl_trust_store;

	@Value("${server.ssl.trust-store-password}")
    private String server_ssl_trust_store_password;
	
}
