package com.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
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
				@Server(url = "http://localhost:8080", description = "Uat Environment"),
				@Server(url = "http://localhost:8080", description = "Production Environment")
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
@SecurityScheme(
		name = "api-key",                     // Optional fallback
		type = SecuritySchemeType.APIKEY,     // API Key auth
		paramName = "X-API-KEY",              // Header name
		in = SecuritySchemeIn.HEADER,         // Location of the key
		description = "Static API key for trusted integrations"
		)
@SecurityScheme(
		name = "oauth2",                      // Optional future support
		type = SecuritySchemeType.OAUTH2,
		flows = @OAuthFlows(
				authorizationCode = @OAuthFlow(
						authorizationUrl = "https://auth.example.com/oauth/authorize",
						tokenUrl = "https://auth.example.com/oauth/token",
						scopes = {
								@OAuthScope(name = "read:data", description = "Read access"),
								@OAuthScope(name = "write:data", description = "Write access")
						}
						)
				),
		description = "OAuth2 with Authorization Code flow"
		)
@Profile("prod")
public class OpenApiConfig {}
