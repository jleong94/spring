package com.configuration;

import java.util.List;
import java.util.UUID;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import com.exception.UnauthenticatedAccessException;
import com.service.UserService;
import com.properties.Property;

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
	UserService userService;
	
	@Autowired
	Property property;
	
	@Autowired
	RateLimitInterceptor rateLimitInterceptor;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) {
		MDC.put("mdcId", UUID.randomUUID());
		try {
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
					.authorizeHttpRequests((requests) -> requests
							.requestMatchers("/v1/user/registration").permitAll()
							.requestMatchers("/v1/reset/password").permitAll()
							.requestMatchers("/v1/oauth-token").permitAll()
							.requestMatchers("/v1/email/**").authenticated()
							.anyRequest().denyAll() // catch-all fallback, block any unspecified endpoints
							)
					// Custom authentication provider and filters
					.authenticationProvider(authenticationProvider())
					.addFilterBefore(securityFilter, BasicAuthenticationFilter.class)
					.build();
		} catch(Exception e) {
			// Get the current stack trace element
			StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
			// Find matching stack trace element from exception
			for (StackTraceElement element : e.getStackTrace()) {
				if (currentElement.getClassName().equals(element.getClassName())
						&& currentElement.getMethodName().equals(element.getMethodName())) {
					log.error("Error in {} at line {}: {} - {}",
							element.getClassName(),
							element.getLineNumber(),
							e.getClass().getName(),
							e.getMessage());
					break;
				}
			}
		} finally {MDC.clear();}
		return null;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(userDetailsService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return userService;
	}

	/*
	 * @param3 Parallelism (number of threads)
	 * @param4 Memory in KB
	 * @param5 Number of iterations (higher = more secure but slower)
	 * */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new Argon2PasswordEncoder(16, 32, 1, 65536, 10);
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
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
}
