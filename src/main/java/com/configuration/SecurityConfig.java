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
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
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

import jakarta.ws.rs.HttpMethod;
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
	CustomOncePerRequestFilter customOncePerRequestFilter;
	
	@Autowired
	Property property;
	
	@Autowired
	CustomHandlerInterceptor customHandlerInterceptor;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.headers(headers -> {
					// Force HTTPS communication and prevent protocol downgrade attacks.
					if (sslEnabled) {
						headers.httpStrictTransportSecurity(hsts -> hsts
								.includeSubDomains(true)// Applies HSTS to subdomains
								.maxAgeInSeconds(31536000)// Cache duration: 1 year
								);
					}
					 // Mitigate XSS, clickjacking, and content injection attacks
					headers.contentSecurityPolicy(csp -> csp
							.policyDirectives("default-src 'self'; script-src 'self'; object-src 'none'; frame-ancestors 'none';")
							);
					//Prevent the site from being embedded in frames (defense against clickjacking)
					headers.frameOptions(frame -> frame.deny());
					//Referrer policy to prevent leaking full URLs when navigating offsite
					headers.referrerPolicy(referrer -> referrer
							.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)
							);
				})

				// Disable CSRF protection because app is stateless (e.g., uses JWT tokens)
				.csrf(csrf -> csrf.disable())
				// Enable Cross-Origin Resource Sharing with custom config
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				// Stateless session management: no HTTP session stored server-side
				.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
						)
				// Handle authentication & access errors with custom exceptions
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint((request, response, authEx) -> {
							//Fired when an unauthenticated request tries to access a protected resource
							throw new UnauthenticatedAccessException("Unauthorized access.");
						})
						.accessDeniedHandler((req, res, accessDeniedEx) -> {
							//Fired when an authenticated user lacks required permissions
							throw new UnauthenticatedAccessException("Access denied.");
						})
						)
				// Register custom filter before Spring Security’s BasicAuthenticationFilter
				.addFilterBefore(customOncePerRequestFilter, BasicAuthenticationFilter.class)
				// Secure endpoint access rules
				.authorizeHttpRequests((requests) -> requests
						.requestMatchers(HttpMethod.POST, "/v1/rate-limits/update").hasAnyAuthority("SCOPE_admin_rate_limit_write")
						.requestMatchers(HttpMethod.POST, "/v1/auth/maintenance").hasAnyAuthority("SCOPE_user_user_maintenance_write", "SCOPE_admin_user_maintenance_write")
						.requestMatchers(HttpMethod.GET, "/v1/auth/check/**").hasAnyAuthority("SCOPE_user_query_user_read", "SCOPE_admin_user_maintenance_write")
						.anyRequest().permitAll() // All other endpoints are publicly accessible
						)
				// Configure OAuth2 resource server to validate JWT tokens
				.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> 
				jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()) // Customize authentication conversion
						))
				.build();// Return the built filter chain
	}
	
	/**
     * Configures how authorities (roles) are extracted from the JWT token.
     * Uses a custom CustomConverter to extract and convert "realm_access.roles".
     */
	@Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		jwtGrantedAuthoritiesConverter.setAuthorityPrefix("SCOPE_"); // Needed for hasAuthority("SCOPE_xxx")
		jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("scope");
		
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
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
        registry.addInterceptor(customHandlerInterceptor)
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
		String jwkSetUri = property.getKeycloak_base_url().concat("/realms/").concat(property.getKeycloak_realm()).concat("/protocol/openid-connect/certs");
		// Build decoder that uses public keys from Keycloak
		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
		// Validate the token issuer (Keycloak realm)
		OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(property.getKeycloak_base_url().concat("/realms/").concat(property.getKeycloak_realm()));
		jwtDecoder.setJwtValidator(withIssuer);
		return jwtDecoder;
	}
}
