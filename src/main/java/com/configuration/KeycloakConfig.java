package com.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Builder;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "keycloak")
@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non_final fields, and @RequiredArgsConstructor
@Builder(toBuilder = true)
public class KeycloakConfig {

	private String auth_server_url;

	private String realm;

	private String client_id;

	private String client_secret;
	
	public String getTokenEndpoint() {
        return String.format("%s/realms/%s/protocol/openid-connect/token", auth_server_url, realm);
    }
    
    public String getUserInfoEndpoint() {
        return String.format("%s/realms/%s/protocol/openid-connect/userinfo", auth_server_url, realm);
    }
}
