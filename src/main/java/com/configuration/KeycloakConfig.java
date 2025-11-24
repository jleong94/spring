package com.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "keycloak")
@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non_final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@Builder(toBuilder = true)
public class KeycloakConfig {

	@Value("auth_server_url")
	private String auth_server_url;

	@Value("realm")
	private String realm;

	@Value("client_id")
	private String client_id;

	@Value("client_secret")
	private String client_secret;
	
	public String getTokenEndpoint() {
        return String.format("%s/realms/%s/protocol/openid-connect/token", auth_server_url, realm);
    }
    
    public String getUserInfoEndpoint() {
        return String.format("%s/realms/%s/protocol/openid-connect/userinfo", auth_server_url, realm);
    }
}
