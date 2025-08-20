package com.service.keycloak;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.springframework.cache.annotation.Cacheable;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.FederatedIdentityRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.ws.rs.core.Response;

import com.exception.DuplicatedEmailException;
import com.exception.DuplicatedUsernameException;
import com.exception.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pojo.Jwt;
import com.pojo.Property;
import com.pojo.keycloak.Access;
import com.pojo.keycloak.Credential;
import com.pojo.keycloak.FederatedIdentitie;
import com.pojo.keycloak.User;
import com.service.MTLSCertificationDetectionService;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.Cleanup;

@Service
public class KeycloakService {
	
	@Autowired
	Property property;
	
	@Autowired
	MTLSCertificationDetectionService mTlsCertificationDetectionService;
	
	private final Keycloak keycloak;
	
	KeycloakService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }
	
	@Cacheable("keycloak-access-token")
	public Jwt requestAccessTokenViaPassword(Logger log, String logFolder, Jwt jwt) throws Exception {
		AccessTokenResponse result = new AccessTokenResponse();
		try {
			Keycloak keycloak = KeycloakBuilder.builder()
		            .serverUrl(property.getKeycloak_base_url())
		            .realm(property.getKeycloak_realm())
		            .clientId(property.getKeycloak_client_id())
		            .username(jwt.getUsername())
		            .password(jwt.getPassword())
		            .grantType(OAuth2Constants.PASSWORD)
		            .build();
			result = keycloak.tokenManager().getAccessToken();
			if(result.getErrorDescription() != null && !result.getErrorDescription().isBlank()) {
				throw new RuntimeException(result.getErrorDescription());
			} if(result.getError() != null && !result.getError().isBlank()) {
				throw new RuntimeException(result.getError());
			} else {
				jwt.toBuilder()
				.access_token(result.getToken())
				.expires_in(result.getExpiresIn())
				.refresh_token(result.getRefreshToken())
				.refresh_expires_in(result.getRefreshExpiresIn())
				.build();
			}
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
			throw e;
		} finally {
			try {
				
			}catch(Exception e) {}
		}
		return jwt;
	}
	
	@Cacheable("keycloak-refresh-token")
	public Jwt requestAccessTokenViaRefreshToken(Logger log, String logFolder, Jwt jwt) throws Exception {
		String URL = "";
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule());
		try {
			URL = property.getKeycloak_base_url().concat("/realms/").concat(property.getKeycloak_realm()).concat("/protocol/openid-connect/token");
			log.info("URL: " + URL);
			//log.info("Request: " + objectMapper.writeValueAsString(object));
			if(URL != null && !URL.isBlank()){
				URI uri = URI.create(URL);
				String host = uri.getHost();
		        int port = uri.getPort() == -1 ? 443 : uri.getPort();
		        boolean mtls = mTlsCertificationDetectionService.isMTLSActive(host, port);
		        Map<String, X509Certificate[]> certChains = mTlsCertificationDetectionService.loadClientCertChains(log, property.getServer_ssl_key_store(), property.getServer_ssl_key_store_password());
		        SSLContext sslContext = SSLContext.getInstance("TLS");//TLS is general name, which version to pickup is depend on JVM setting
		        if (mtls && certChains.size() > 1) {
		            log.info("mTLS active and multiple certs found — enabling smart selection");
		            sslContext = mTlsCertificationDetectionService.createSSLContext(log, property.getServer_ssl_key_store(), property.getServer_ssl_key_store_password(), property.getServer_ssl_trust_store(), property.getServer_ssl_trust_store_password(), false);
		        } else {sslContext = mTlsCertificationDetectionService.createSSLContext(log, property.getServer_ssl_key_store(), property.getServer_ssl_key_store_password(), property.getServer_ssl_trust_store(), property.getServer_ssl_trust_store_password(), true);}
				List<NameValuePair> params = new ArrayList<>();
				params.add(new BasicNameValuePair("client_id", property.getKeycloak_client_id()));
				params.add(new BasicNameValuePair("grant_type", jwt.getGrant_type()));
				params.add(new BasicNameValuePair("refresh_token", jwt.getRefresh_token()));
				RequestConfig requestConfig = RequestConfig.custom()
		                .setConnectTimeout(5 * 1000)//in miliseconds
		                .setSocketTimeout(5 * 1000)//in miliseconds
		                .setConnectionRequestTimeout(5 * 1000)//in miliseconds
		                .build();
				@Cleanup CloseableHttpClient httpClient = HttpClients.custom()
						.setSSLContext(sslContext)
		                .setDefaultRequestConfig(requestConfig)
		                .build();
				HttpPost httpRequest = new HttpPost(URL);
				/*HttpGet httpRequest = new HttpGet(URL);*/
				uri = new URIBuilder(httpRequest.getURI())
						.addParameters(params)
						.build();
				httpRequest.setURI(uri);
				//HttpPut httpRequest = new HttpPut(URL);
				//HttpDelete httpRequest = new HttpDelete(URL);
				//httpRequest.setEntity(new StringEntity(objectMapper.writeValueAsString(object)));
				httpRequest.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
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
//						Read & update the response JSON parameter value into Object
						jwt = objectMapper.readValue(responseString, Jwt.class);
					}
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
					throw e;
				}
			}
		} catch(SocketTimeoutException | ConnectTimeoutException e) {
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
			throw e;
		} finally {
			try {
				
			}catch(Exception e) {}
		}
		return jwt;
	}

	public User userCreation(Logger log, String logFolder, User user) throws Exception {
		try {
			int totalUsers = keycloak.realm(property.getKeycloak_realm())
				    .users()
				    .count();
			if(checkIfUsernameExisted(log, logFolder, user)) {
				throw new DuplicatedUsernameException("Duplicated username found.");
			} if(checkIfEmailExisted(log, logFolder, user)) {
				throw new DuplicatedEmailException("Duplicated email found.");
			}
			UserRepresentation userRepresentation = new UserRepresentation();
			if(user.getUsername() != null && !user.getUsername().isBlank()) {
				userRepresentation.setUsername(user.getUsername());
			} 
			userRepresentation.setEnabled(user.isEnabled());
			userRepresentation.setEmailVerified(user.isEmailVerified());
			if(user.getEmail() != null && !user.getEmail().isBlank()) {
				userRepresentation.setEmail(user.getEmail());
			} if(user.getFirstName() != null && !user.getFirstName().isBlank()) {
				userRepresentation.setFirstName(user.getFirstName());
			} if(user.getLastName() != null && !user.getLastName().isBlank()) {
				userRepresentation.setLastName(user.getLastName());
			} if(user.getAttributes() != null && user.getAttributes().size() > 0) {
				userRepresentation.setAttributes(user.getAttributes());
			} if (user.getCredentials() != null && user.getCredentials().size() > 0) {
				// Set password
				List<CredentialRepresentation> creds = user.getCredentials().stream()
						.map(c -> {
							CredentialRepresentation cr = new CredentialRepresentation();
							cr.setType(c.getType());
							cr.setValue(c.getValue());
							cr.setTemporary(c.isTemporary());
							return cr;
						}).collect(Collectors.toList());
				userRepresentation.setCredentials(creds);
			} if(user.getDisableableCredentialTypes() != null && user.getDisableableCredentialTypes().size() > 0) {
				userRepresentation.setDisableableCredentialTypes(user.getDisableableCredentialTypes());
			} if(user.getRequiredActions() != null && user.getRequiredActions().size() > 0) {
				userRepresentation.setRequiredActions(user.getRequiredActions());
			} if (user.getFederatedIdentities() != null && user.getFederatedIdentities().size() > 0) {
				List<FederatedIdentityRepresentation> federatedReps = user.getFederatedIdentities().stream()
						.map(f -> {
							FederatedIdentityRepresentation fir = new FederatedIdentityRepresentation();
							fir.setIdentityProvider(f.getIdentityProvider());
							fir.setUserId(f.getUserId());
							fir.setUserName(f.getUserName());
							return fir;
						}).collect(Collectors.toList());
				userRepresentation.setFederatedIdentities(federatedReps);
			} if(user.getRealmRoles() != null && user.getRealmRoles().size() > 0) {
				userRepresentation.setRealmRoles(user.getRealmRoles());
			} if(user.getClientRoles() != null && user.getClientRoles().size() > 0) {
				userRepresentation.setClientRoles(user.getClientRoles());
			} if(user.getGroups() != null && user.getGroups().size() > 0) {
				userRepresentation.setGroups(user.getGroups());
			} if(user.getServiceAccountClientId() != null && !user.getServiceAccountClientId().isBlank()) {
				userRepresentation.setServiceAccountClientId(user.getServiceAccountClientId());
			} if(user.getSelf() != null && !user.getSelf().isBlank()) {
				userRepresentation.setSelf(user.getSelf());
			} if(user.getCreatedTimestamp() > 0) {
				userRepresentation.setCreatedTimestamp(user.getCreatedTimestamp());
			} if(user.getAccess() != null) {
				Map<String,Boolean> access = new HashMap<>();
				access.put("manageGroupMembership", user.getAccess().isManageGroupMembership());
				access.put("view", user.getAccess().isView());
				access.put("mapRoles", user.getAccess().isMapRoles());
				access.put("impersonate", user.getAccess().isImpersonate());
				access.put("manage", user.getAccess().isManage());
				userRepresentation.setAccess(access);
			} if(user.getNotBefore() > 0) {
				userRepresentation.setNotBefore(user.getNotBefore());
			}

			@Cleanup Response response = keycloak.realm(property.getKeycloak_realm()).users().create(userRepresentation);
			MultivaluedMap<String, Object> headers = response.getHeaders();
			for (Map.Entry<String, List<Object>> entry : headers.entrySet()) {
				String headerName = entry.getKey();
				List<Object> headerValues = entry.getValue();
				log.info(headerName + "(Response): " + headerValues);
			}
			log.info("HTTP Response code: " + response.getStatus());
			if(response.getStatus() == 201) {
				String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
				user.setId(userId);
				userRoleMaintenance(log, logFolder, user, totalUsers);
			} else {
				throw new RuntimeException("Error, ".concat(response.readEntity(String.class)).concat(" while creating user."));
			}
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
			throw e;
		} finally {
			try {

			}catch(Exception e) {}
		}
		return user;
	}

	public User userMaintenance(Logger log, String logFolder, User user) throws Exception {
		List<UserRepresentation> userRepresentations = new ArrayList<>();
		UserRepresentation userRepresentation = new UserRepresentation();
		try {
			if(user.getId() == null || user.getId().isBlank()) {
				userRepresentations = keycloak.realm(property.getKeycloak_realm())
						.users()
						.search(user.getUsername(), true);
				userRepresentation = userRepresentations.get(0);
			} else {userRepresentation = keycloak.realm(property.getKeycloak_realm()).users().get(user.getId()).toRepresentation();}
			
			if(user.getUsername() != null && !user.getUsername().isBlank()) {
				if(!user.getUsername().equals(userRepresentation.getUsername())) {
					if(checkIfUsernameExisted(log, logFolder, user)) {
						throw new DuplicatedUsernameException("Duplicated username found.");
					}
				}
				userRepresentation.setUsername(user.getUsername());
			} 
			userRepresentation.setEnabled(user.isEnabled());
			userRepresentation.setEmailVerified(user.isEmailVerified());
			if(user.getEmail() != null && !user.getEmail().isBlank()) {
				if(!user.getEmail().equals(userRepresentation.getEmail())) {
					if(checkIfEmailExisted(log, logFolder, user)) {
						throw new DuplicatedEmailException("Duplicated email found.");
					}
				}
				userRepresentation.setEmail(user.getEmail());
			} if(user.getFirstName() != null && !user.getFirstName().isBlank()) {
				userRepresentation.setFirstName(user.getFirstName());
			} if(user.getLastName() != null && !user.getLastName().isBlank()) {
				userRepresentation.setLastName(user.getLastName());
			} if(user.getAttributes() != null && user.getAttributes().size() > 0) {
				userRepresentation.setAttributes(user.getAttributes());
			} if (user.getCredentials() != null && user.getCredentials().size() > 0) {
				// Set password
				List<CredentialRepresentation> creds = user.getCredentials().stream()
						.map(c -> {
							CredentialRepresentation cr = new CredentialRepresentation();
							cr.setType(c.getType());
							cr.setValue(c.getValue());
							cr.setTemporary(c.isTemporary());
							return cr;
						}).collect(Collectors.toList());
				userRepresentation.setCredentials(creds);
			} if(user.getDisableableCredentialTypes() != null && user.getDisableableCredentialTypes().size() > 0) {
				userRepresentation.setDisableableCredentialTypes(user.getDisableableCredentialTypes());
			} if(user.getRequiredActions() != null && user.getRequiredActions().size() > 0) {
				userRepresentation.setRequiredActions(user.getRequiredActions());
			} if (user.getFederatedIdentities() != null && user.getFederatedIdentities().size() > 0) {
				List<FederatedIdentityRepresentation> federatedReps = user.getFederatedIdentities().stream()
						.map(f -> {
							FederatedIdentityRepresentation fir = new FederatedIdentityRepresentation();
							fir.setIdentityProvider(f.getIdentityProvider());
							fir.setUserId(f.getUserId());
							fir.setUserName(f.getUserName());
							return fir;
						}).collect(Collectors.toList());
				userRepresentation.setFederatedIdentities(federatedReps);
			} if(user.getRealmRoles() != null && user.getRealmRoles().size() > 0) {
				userRepresentation.setRealmRoles(user.getRealmRoles());
			} if(user.getClientRoles() != null && user.getClientRoles().size() > 0) {
				userRepresentation.setClientRoles(user.getClientRoles());
			} if(user.getGroups() != null && user.getGroups().size() > 0) {
				userRepresentation.setGroups(user.getGroups());
			} if(user.getServiceAccountClientId() != null && !user.getServiceAccountClientId().isBlank()) {
				userRepresentation.setServiceAccountClientId(user.getServiceAccountClientId());
			} if(user.getSelf() != null && !user.getSelf().isBlank()) {
				userRepresentation.setSelf(user.getSelf());
			} if(user.getCreatedTimestamp() > 0) {
				userRepresentation.setCreatedTimestamp(user.getCreatedTimestamp());
			} if(user.getAccess() != null) {
				Map<String,Boolean> access = new HashMap<>();
				access.put("manageGroupMembership", user.getAccess().isManageGroupMembership());
				access.put("view", user.getAccess().isView());
				access.put("mapRoles", user.getAccess().isMapRoles());
				access.put("impersonate", user.getAccess().isImpersonate());
				access.put("manage", user.getAccess().isManage());
				userRepresentation.setAccess(access);
			} if(user.getNotBefore() > 0) {
				userRepresentation.setNotBefore(user.getNotBefore());
			}
			
			keycloak.realm(property.getKeycloak_realm()).users().get(userRepresentation.getId()).update(userRepresentation);
		} catch (NotFoundException e) {
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
			throw new UserNotFoundException("User info not found.");
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
			throw e;
		} finally {
			try {
				
			}catch(Exception e) {}
		}
		return user;
	}

	public boolean userRoleMaintenance(Logger log, String logFolder, User user, int totalUsers) throws Exception {
		List<UserRepresentation> userRepresentations = new ArrayList<>();
		UserRepresentation userRepresentation = new UserRepresentation();
		try {
			if(user.getId() == null || user.getId().isBlank()) {
				userRepresentations = keycloak.realm(property.getKeycloak_realm())
						.users()
						.search(user.getUsername(), true);
				userRepresentation = userRepresentations.get(0);
			} else {userRepresentation = keycloak.realm(property.getKeycloak_realm()).users().get(user.getId()).toRepresentation();}
			List<String> permissions = Arrays.asList("read", "write");
			List<String> actions = Arrays.asList("user_maintenance", "query_user");
			if(totalUsers <= 0) {actions.add("rate_limit");}
			List<RoleRepresentation> roleRepresentations = new ArrayList<>();
			for(String action : actions) {
				for(String permission : permissions) {
					try {
						roleRepresentations.add(keycloak.realm(property.getKeycloak_realm()).clients()
								.get(property.getKeycloak_client_id())
								.roles().get((totalUsers <= 0 ? "admin" : "user").concat("_").concat(action).concat("_").concat(permission))
								.toRepresentation());
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
					}
				}
			}
			keycloak.realm(property.getKeycloak_realm()).users().get(userRepresentation.getId()).roles().clientLevel(property.getKeycloak_client_id()).add(roleRepresentations);
			return true;
		} catch (NotFoundException e) {
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
			throw new UserNotFoundException("User info not found.");
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
			throw e;
		} finally {
			try {
				
			}catch(Exception e) {}
		}
	}

	public User getUserDetailByUsernameOrId(Logger log, String logFolder, User user) throws Exception {
		List<UserRepresentation> userRepresentations = new ArrayList<>();
		UserRepresentation userRepresentation = new UserRepresentation();
		try {
			if(user.getId() == null || user.getId().isBlank()) {
				userRepresentations = keycloak.realm(property.getKeycloak_realm())
						.users()
						.search(user.getUsername(), true);
				userRepresentation = userRepresentations.get(0);
			} else {userRepresentation = keycloak.realm(property.getKeycloak_realm()).users().get(user.getId()).toRepresentation();}
			Map<String, Boolean> access = userRepresentation.getAccess();
			List<Credential> credentials = Optional.ofNullable(userRepresentation.getCredentials())
					.orElse(Collections.emptyList())
					.stream()
					.map(c -> {
						Credential credential = new Credential();
						credential.setType(c.getType());
						credential.setValue(c.getValue());
						credential.setTemporary(c.isTemporary());
						return credential;
					})
					.collect(Collectors.toList());
			List<FederatedIdentitie> federatedIdentities = Optional.ofNullable(userRepresentation.getFederatedIdentities())
					.orElse(Collections.emptyList())
					.stream()
					.map(f -> {
						FederatedIdentitie fi = new FederatedIdentitie();
						fi.setIdentityProvider(f.getIdentityProvider());
						fi.setUserId(f.getUserId());
						fi.setUserName(f.getUserName());
						return fi;
					})
					.collect(Collectors.toList());

			user.toBuilder()
			.id(userRepresentation.getId())
			.username(userRepresentation.getUsername())
			.enabled(userRepresentation.isEnabled())
			.emailVerified(userRepresentation.isEmailVerified())
			.email(userRepresentation.getEmail())
			.firstName(userRepresentation.getFirstName())
			.lastName(userRepresentation.getLastName())
			.attributes(userRepresentation.getAttributes())
			.credentials(credentials)
			.disableableCredentialTypes(userRepresentation.getDisableableCredentialTypes())
			.requiredActions(userRepresentation.getRequiredActions())
			.federatedIdentities(federatedIdentities)
			.realmRoles(userRepresentation.getRealmRoles())
			.clientRoles(userRepresentation.getClientRoles())
			.groups(userRepresentation.getGroups())
			.serviceAccountClientId(userRepresentation.getServiceAccountClientId())
			.self(userRepresentation.getSelf())
			.createdTimestamp(userRepresentation.getCreatedTimestamp())
			.access(Access.builder()
					.manageGroupMembership(access.getOrDefault("manageGroupMembership", false))
					.view(access.getOrDefault("view", false))
					.mapRoles(access.getOrDefault("mapRoles", false))
					.impersonate(access.getOrDefault("impersonate", false))
					.manage(access.getOrDefault("manage", false))
					.build())
			.notBefore(userRepresentation.getNotBefore())
			.build();
		} catch (NotFoundException e) {
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
			throw new UserNotFoundException("User info not found.");
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
			throw e;
		} finally {
			try {
				
			}catch(Exception e) {}
		}
		return user;
	}
	
	private boolean checkIfUsernameExisted(Logger log, String logFolder, User user) {
		try {
			List<UserRepresentation> userRepresentations = keycloak.realm(property.getKeycloak_realm())
					.users()
					.search(user.getUsername(), true);
			return userRepresentations.size() > 0;
		} catch (NotFoundException e) {
			return false;
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
			return true;
		}
	}
	
	private boolean checkIfEmailExisted(Logger log, String logFolder, User user) {
		try {
			List<UserRepresentation> userRepresentations = keycloak.realm(property.getKeycloak_realm())
					.users()
					.searchByEmail(user.getEmail(), true);
			return userRepresentations.size() > 0;
		} catch (NotFoundException e) {
			return false;
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
			return true;
		}
	}
}
