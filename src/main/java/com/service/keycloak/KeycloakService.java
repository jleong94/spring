package com.service.keycloak;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.configuration.KeycloakConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modal.Email;
import com.pojo.Property;
import com.pojo.keycloak.AuthRequest;
import com.pojo.keycloak.AuthResponse;
import com.pojo.keycloak.RefreshTokenRequest;
import com.service.EmailService;
import com.service.MTLSCertificationDetectionService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Cleanup;

@Service
public class KeycloakService {

	private static final int CONNECT_TIMEOUT_MS = 5000;
	private static final int SOCKET_TIMEOUT_MS = 5000;
	private static final int CONNECTION_REQUEST_TIMEOUT_MS = 5000;
	private static final int DEFAULT_HTTPS_PORT = 443;

	private final ObjectMapper objectMapper;

	private final MTLSCertificationDetectionService mTlsCertificationDetectionService;

	private final Property property;

	private final EmailService emailService;

	private final MeterRegistry meterRegistry;

	private final Counter recoverCounter;

	private final RestTemplate restTemplate = new RestTemplate();

	private final KeycloakConfig keycloakConfig;

	public KeycloakService(ObjectMapper objectMapper, MTLSCertificationDetectionService mTlsCertificationDetectionService, Property property, EmailService emailService, MeterRegistry meterRegistry, KeycloakConfig keycloakConfig) {
		this.objectMapper = objectMapper;
		this.mTlsCertificationDetectionService = mTlsCertificationDetectionService;
		this.property = property;
		this.emailService = emailService;
		this.meterRegistry = meterRegistry;
		this.recoverCounter = Counter.builder("auth_service_failures_total")
				.description("Number of failed retries hitting @Recover")
				.register(this.meterRegistry);
		this.keycloakConfig = keycloakConfig;
	}

	@Retry(name = "requestAuthToken")
	@CircuitBreaker(name = "requestAuthToken", fallbackMethod = "fallbackRequestAuthToken")
	public AuthResponse requestAuthToken(Logger log, AuthRequest authRequest, RefreshTokenRequest refreshTokenRequest) throws Throwable {
		AuthResponse result = null;
		try {
			log.info("URL: " + keycloakConfig.getTokenEndpoint());
			if(keycloakConfig.getTokenEndpoint() != null && !keycloakConfig.getTokenEndpoint().isBlank()){
				URI uri = URI.create(keycloakConfig.getTokenEndpoint());
				String host = uri.getHost();
				int port = uri.getPort() == -1 ? DEFAULT_HTTPS_PORT : uri.getPort();
				// Check if MTLS is required
				boolean mtls = mTlsCertificationDetectionService.isMTLSActive(host, port);
				Map<String, X509Certificate[]> certChains = mTlsCertificationDetectionService.loadClientCertChains(
						log, 
						property.getServer_ssl_key_store(), 
						property.getServer_ssl_key_store_password(), 
						property.getServer_ssl_key_store_type()
						);
				// Create SSL context with smart cert selection if needed
				boolean useSmartSelection = mtls && certChains.size() > 1;
				if (useSmartSelection) {
					log.info("MTLS active and multiple certs found — enabling smart selection");
				}
				SSLContext sslContext = mTlsCertificationDetectionService.createSSLContext(
						log, 
						property.getServer_ssl_protocol(), 
						property.getServer_ssl_key_store(), 
						property.getServer_ssl_key_store_password(), 
						property.getServer_ssl_key_store_type(), 
						property.getServer_ssl_trust_store(), 
						property.getServer_ssl_trust_store_password(), 
						property.getServer_ssl_trust_store_type(), 
						!useSmartSelection, 
						null
						);
				// Enforce TLS versions + hostname verification
				SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
						sslContext,
						property.getServer_ssl_enabled_protocols(),
						null,
						SSLConnectionSocketFactory.getDefaultHostnameVerifier()
						);
				List<NameValuePair> params = new ArrayList<>();
				params.add(new BasicNameValuePair("grant_type", refreshTokenRequest != null ? "refresh_token" : "password"));
				params.add(new BasicNameValuePair("client_id", keycloakConfig.getClient_id()));
				params.add(new BasicNameValuePair("client_secret", keycloakConfig.getClient_secret()));
				if(refreshTokenRequest != null) {
					params.add(new BasicNameValuePair("refresh_token", refreshTokenRequest.getRefresh_token()));
				} else {
					params.add(new BasicNameValuePair("username", authRequest.getUsername()));
					params.add(new BasicNameValuePair("password", authRequest.getPassword()));
				}
				RequestConfig requestConfig = RequestConfig.custom()
						.setConnectTimeout(CONNECT_TIMEOUT_MS)//in miliseconds
						.setSocketTimeout(SOCKET_TIMEOUT_MS)//in miliseconds
						.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MS)//in miliseconds
						.build();
				@Cleanup CloseableHttpClient httpClient = HttpClients.custom()
						.setSSLSocketFactory(sslConnectionSocketFactory)
						.setSSLContext(sslContext)
						.setDefaultRequestConfig(requestConfig)
						//.setConnectionManager()
						.build();
				HttpPost httpRequest = new HttpPost(keycloakConfig.getTokenEndpoint());
				/*HttpGet httpRequest = new HttpGet(URL);*/
				uri = new URIBuilder(httpRequest.getURI())
						.addParameters(params)
						.build();
				httpRequest.setURI(uri);
				log.info("Request: ".concat(httpRequest.getURI().toString()));
				//HttpPut httpRequest = new HttpPut(URL);
				//HttpDelete httpRequest = new HttpDelete(URL);
				//httpRequest.setEntity(new StringEntity(objectMapper.writeValueAsString(object)));
				httpRequest.setHeader("Content-Type", "application/json; charset=UTF-8");
				for(Header header : httpRequest.getAllHeaders()) {
					log.info(header.getName() + "(Request): " + header.getValue());
				}
				@Cleanup CloseableHttpResponse httpResponse = httpClient.execute(httpRequest);
				for(Header header : httpResponse.getAllHeaders()) {
					log.info(header.getName() + "(Response): " + header.getValue());
				}
				HttpEntity entity = httpResponse.getEntity();
				log.info("HTTP Response code: " + httpResponse.getStatusLine().getStatusCode());
				try {
					if(entity != null) {
						String responseString = EntityUtils.toString(entity);
						log.info("Response: " + responseString);
						// Read the response JSON parameter value & put into object as new instance
						result = objectMapper.readValue(responseString, AuthResponse.class);
						// Extract roles from access token
			            Set<String> roles = extractRolesFromToken(log, result.getAccess_token());
			            result = result.toBuilder()
			            		.username(authRequest.getUsername())
			            		.roles(roles)
			            		.build();
					}
				} catch(Throwable e) {
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
				}

			}
		} catch(Throwable e) {
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
			throw e;
		} finally {
			try {

			}catch(Throwable e) {}
		}
		return result;
	}

	// Exception param must put as last param in fallback method
	public void fallbackRequestAuthToken(Logger log, AuthRequest authRequest, Throwable throwable) {
		log.info("Recover on request auth token start.");
		try {
			String error_detail = "";
			StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
			for (StackTraceElement element : throwable.getStackTrace()) {
				if (currentElement.getClassName().equals(element.getClassName())) {
					error_detail += (error_detail != null && !error_detail.isBlank() ? "<br>" : "") + String.format("Error in %s at line %d: %s - %s",
							element.getClassName(),
							element.getLineNumber(),
							throwable.getClass().getName(),
							throwable.getMessage());
					break;
				}
			}
			// Increment Prometheus counter
			recoverCounter.increment();

			// Persist failure details

			// Trigger alerts (Ops team, monitoring system)
			if(property.getAlert_slack_webhook_url() != null && !property.getAlert_slack_webhook_url().isBlank()) {
				restTemplate.postForEntity(property.getAlert_slack_webhook_url(), java.util.Collections.singletonMap("text", error_detail), String.class);
			} if((property.getAlert_support_email_to() != null && !property.getAlert_support_email_to().isBlank()) ||
					(property.getAlert_support_email_cc() != null && !property.getAlert_support_email_cc().isBlank()) ||
					(property.getAlert_support_email_bcc() != null && !property.getAlert_support_email_bcc().isBlank())) {
				String exceptionNotificationEmailTemplate = String.format(emailService.loadHtmlTemplate(log, "keycloak_exception.html"), property.getSpring_application_name(), property.getSpring_application_name(), error_detail, property.getSpring_application_name());
				Email email = Email.builder()
						.sender(property.getSpring_mail_sender())
						.replyTo(property.getAlert_support_email_replyTo())
						.receiver(property.getAlert_support_email_to())
						.cc(property.getAlert_support_email_cc())
						.bcc(property.getAlert_support_email_bcc())
						.subject("System Exception Error!!!")
						.body(exceptionNotificationEmailTemplate)
						.build();
				emailService.sendEmail(log, email);
			}
		} catch(Throwable e) {
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
		} finally{
			log.info("Recover on request auth token end.");
		}
	}

	/**
	 * Extract roles from JWT token
	 */
	private Set<String> extractRolesFromToken(Logger log, String token) {
		try {
			// Decode JWT payload (simple Base64 decode - in production, use proper JWT library)
			String[] parts = token.split("\\.");
			if (parts.length < 2) {
				return Collections.emptySet();
			}

			String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

			// Parse roles from realm_access and resource_access
			Set<String> roles = new HashSet<>();

			// This is a simple extraction - in production, use Jackson or a JWT library
			if (payload.contains("realm_access")) {
				// Extract realm roles
				int realmStart = payload.indexOf("\"realm_access\"");
				int rolesStart = payload.indexOf("\"roles\"", realmStart);
				int rolesEnd = payload.indexOf("]", rolesStart);
				if (rolesStart > 0 && rolesEnd > rolesStart) {
					String rolesSection = payload.substring(rolesStart, rolesEnd);
					// Simple extraction - improve with proper JSON parsing
					Arrays.stream(rolesSection.split("\""))
					.filter(s -> !s.matches("[\\[\\],:\\s]+") && !s.equals("roles"))
					.forEach(roles::add);
				}
			}

			return roles;
		} catch (Throwable e) {
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
			return Collections.emptySet();
		}
	}
}
