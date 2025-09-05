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
	CustomOncePerRequestFilter customOncePerRequestFilter;
	
	@Autowired
	Property property;
	
	@Autowired
	CustomHandlerInterceptor customHandlerInterceptor;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Throwable {
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
						//.requestMatchers(HttpMethod.POST, "<endpoint - example, /v1/test>").hasAnyAuthority("SCOPE_<user type>_<action>_<permission: read/write>")
						.anyRequest().permitAll() // All other endpoints are publicly accessible
						)
				.build();// Return the built filter chain
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
}
