package com.configuration;

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

import com.service.UserService;

import lombok.extern.slf4j.Slf4j;

/*
 * Here is to configure spring security
 * */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
	
	@Value("${server.ssl.enabled}")
	private boolean sslEnabled;
	
	@Autowired
	SecurityFilter securityFilter;
	
	@Autowired
	UserService userService;
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) {
		MDC.put("mdcId", UUID.randomUUID());
		try {
			return http.csrf(csrf -> csrf.disable())
					.sessionManagement(session ->
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
					)
					.authorizeHttpRequests((requests) -> requests
							.requestMatchers("/v1/user/registration").permitAll()
							.requestMatchers("/v1/reset/password").permitAll()
							.requestMatchers("/v1/oauth-token").permitAll()
							.requestMatchers("/v1/email/**").authenticated())
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
}
