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
	
	@Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakConverter());
        return jwtAuthenticationConverter;
    }

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(property.getAllowed_origins()); // Whitelist only trusted domains
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
	
	@Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**"); // Customize your target paths
    }
	
	@Bean
	JwtDecoder jwtDecoder() {
		String jwkSetUri = property.getKeycloak_base_url().concat(property.getKeycloak_realm()).concat(property.getKeycloak_cert_endpoint());
		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
		OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(property.getKeycloak_base_url().concat(property.getKeycloak_realm()));
		jwtDecoder.setJwtValidator(withIssuer);
		return jwtDecoder;
	}
}
