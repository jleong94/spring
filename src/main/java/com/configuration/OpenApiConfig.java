package com.configuration;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(
		info = @Info(
				title = "Spring Boot Base Project Template",
				version = "0.0.1",
				description = """
						This project serves as a fully working base template for Java Spring Boot application.
						Easily clone and duplicate this project as a solid starting point for building microservices or monoliths.
						""",
						contact = @Contact(
								name = "API Support",
								email = "jleong963@gmail.com",
								url = "https://github.com/jleong94"
								),
						license = @License(
								name = "Apache 2.0",
								url = "https://github.com/jleong94/spring?tab=Apache-2.0-1-ov-file"
								)
				),
		servers = {
				@Server(url = "http://localhost:8080", description = "Local Development"),
				@Server(url = "http://localhost:8080", description = "UAT Environment"),
				@Server(url = "http://localhost:8080", description = "Production Environment")
		},
		security = {
				@SecurityRequirement(name = "bearer-jwt") // Require JWT globally
		}
		)
// -------------------------------
// 🔐 Security Schemes
// -------------------------------
@SecurityScheme(
		name = "bearer-jwt",                  // Used inside @Operation(security = ...)
		type = SecuritySchemeType.HTTP,       // HTTP auth
		scheme = "bearer",                    // "Bearer <token>"
		bearerFormat = "JWT",                 // Hint for Swagger UI
		description = "JWT Bearer token. Format: 'Bearer {token}'"
		)
public class OpenApiConfig {
	
	// 🔹 Group 1: Template APIs
    @Bean
    public GroupedOpenApi templateApi() {
        return GroupedOpenApi.builder()
                .group("templates")
                .packagesToScan("com.api.template")
                .pathsToMatch("/v1/template/**")
                .build();
    }
    
	// 🔹 Group 2: Authentication APIs
	@Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("authentication")
                .packagesToScan("com.api.auth")
                .pathsToMatch("/v1/auth/**")
                .build();
    }
    
	// 🔹 Group 3: Business APIs
    @Bean
    public GroupedOpenApi businessApi() {
        return GroupedOpenApi.builder()
                .group("business")
                .packagesToScan("com.api.business")
                .pathsToMatch("/v1/business/**")
                .build();
    }
}
