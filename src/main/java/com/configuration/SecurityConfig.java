package com.configuration;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import com.exception.UnauthenticatedAccessException;
import com.pojo.Property;

import lombok.extern.slf4j.Slf4j;

/*
 * Here is to configure spring security
 * */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig implements WebMvcConfigurer {

	@Value("${server.ssl.enabled}")
	private boolean sslEnabled;

	@Autowired
	SecurityFilter securityFilter;
	
	@Autowired
	Property property;
	
	@Autowired
	RateLimitInterceptor rateLimitInterceptor;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.headers(headers -> {
					// Force HTTPS communication and prevent protocol downgrade attacks.
					if (sslEnabled) {
						headers.httpStrictTransportSecurity(hsts -> hsts
								.includeSubDomains(true)
								.maxAgeInSeconds(31536000)
								);
					}
					// Prevent XSS, clickjacking, and content injection.
					headers.contentSecurityPolicy(csp -> csp
							.policyDirectives("default-src 'self'; script-src 'self'; object-src 'none'; frame-ancestors 'none';")
							);
					headers.frameOptions(frame -> frame.deny());
					headers.referrerPolicy(referrer -> referrer
							.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)
							);
				})

				// CSRF protection is disabled because we're stateless (JWT tokens, etc.)
				.csrf(csrf -> csrf.disable())
				// CORS Configuration
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				// Stateless sessions
				.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
						)
				// Exception Handling
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint((request, response, authEx) -> {
							throw new UnauthenticatedAccessException("Unauthorized access.");
						})
						.accessDeniedHandler((req, res, accessDeniedEx) -> {
							throw new UnauthenticatedAccessException("Access denied.");
						})
						)
				//Filter handling
				.addFilterBefore(securityFilter, BasicAuthenticationFilter.class)
				//Authentication & authorization handling
				.authorizeHttpRequests((requests) -> requests
						.requestMatchers("/v1/email/**").authenticated()
						.anyRequest().permitAll() // allow all other requests without authentication
						)
				.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> 
				jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
						))
				.build();
	}
	
	/**
     * Configures how authorities (roles) are extracted from the JWT token.
     * Uses a custom KeycloakConverter to extract and convert "realm_access.roles".
     */
	@Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakConverter());
        return jwtAuthenticationConverter;
    }

	/**
     * Defines global CORS configuration.
     * - Restricts origins to those specified in application properties.
     * - Allows common HTTP methods and all headers.
     * - Enables credentials support (e.g., for cookies).
     */
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(property.getAllowed_origins()); // Whitelist only trusted domains
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));// Common HTTP methods
		configuration.setAllowedHeaders(List.of("*"));// Accept all headers
		configuration.setAllowCredentials(true);// Required for session cookies
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);// Apply to all endpoints
		return source;
	}
	
	/**
     * Adds a custom rate limiting interceptor to all endpoints.
     * This enforces per-path rate limit rules defined elsewhere.
     */
	@Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**"); // Customize your target paths
    }
	
	/**
     * Configures the JWT decoder with a JWK Set URI and issuer validation.
     * - Retrieves public keys from Keycloak to verify tokens.
     * - Validates that the token was issued by the expected realm.
     */
	@Bean
	JwtDecoder jwtDecoder() {
		// Construct the JWK set URI: <base-url>/realms/<realm>/protocol/openid-connect/certs
		String jwkSetUri = property.getKeycloak_base_url().concat(property.getKeycloak_realm()).concat(property.getKeycloak_cert_endpoint());
		// Build decoder that uses public keys from Keycloak
		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
		// Validate the token issuer (Keycloak realm)
		OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(property.getKeycloak_base_url().concat(property.getKeycloak_realm()));
		jwtDecoder.setJwtValidator(withIssuer);
		return jwtDecoder;
	}
}
