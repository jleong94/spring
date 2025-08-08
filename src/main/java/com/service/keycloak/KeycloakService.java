package com.service.keycloak;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.FederatedIdentityRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.ws.rs.core.Response;
import com.pojo.Property;
import com.pojo.keycloak.User;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.Cleanup;

@Service
public class KeycloakService {
	
	@Autowired
	Property property;
	
	private final Keycloak keycloak;
	
	KeycloakService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }
	
	@Cacheable("keycloak-token")
	private String requestAdminToken(Logger log, String logFolder) throws Exception {
		String result = "";
		try {
			
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
		return result;
	}

	public User userCreation(Logger log, String logFolder, User user) throws Exception {
		try {
			UserRepresentation userRepresentation = new UserRepresentation();
			userRepresentation.setUsername(user.getUsername());
			userRepresentation.setEnabled(user.isEnabled());
			userRepresentation.setEmailVerified(user.isEmailVerified());
			userRepresentation.setEmail(user.getEmail());
			userRepresentation.setFirstName(user.getFirstName());
			userRepresentation.setLastName(user.getLastName());
			userRepresentation.setAttributes(user.getAttributes());
			userRepresentation.setRequiredActions(user.getRequiredActions());

			if(user.getAttributes() != null && user.getAttributes().size() > 0) {
				userRepresentation.setAttributes(user.getAttributes());
			}

			// Credentials (e.g., password)
			if (user.getCredentials() != null && user.getCredentials().size() > 0) {
				List<CredentialRepresentation> creds = user.getCredentials().stream()
						.map(c -> {
							CredentialRepresentation cr = new CredentialRepresentation();
							cr.setType(c.getType());
							cr.setValue(c.getValue());
							cr.setTemporary(c.isTemporary());
							return cr;
						}).collect(Collectors.toList());
				userRepresentation.setCredentials(creds);
			}

			if(user.getDisableableCredentialTypes() != null && user.getDisableableCredentialTypes().size() > 0) {
				userRepresentation.setDisableableCredentialTypes(user.getDisableableCredentialTypes());
			}

			if(user.getRequiredActions() != null && user.getRequiredActions().size() > 0) {
				userRepresentation.setRequiredActions(user.getRequiredActions());
			}

			// Federated Identities (optional)
			if (user.getFederatedIdentities() != null && user.getFederatedIdentities().size() > 0) {
				List<FederatedIdentityRepresentation> federatedReps = user.getFederatedIdentities().stream()
						.map(f -> {
							FederatedIdentityRepresentation fir = new FederatedIdentityRepresentation();
							fir.setIdentityProvider(f.getIdentityProvider());
							fir.setUserId(f.getUserId());
							fir.setUserName(f.getUserName());
							return fir;
						}).collect(Collectors.toList());
				userRepresentation.setFederatedIdentities(federatedReps);
			}

			if(user.getRealmRoles() != null && user.getRealmRoles().size() > 0) {
				userRepresentation.setRealmRoles(user.getRealmRoles());
			}

			if(user.getClientRoles() != null && user.getClientRoles().size() > 0) {
				userRepresentation.setClientRoles(user.getClientRoles());
			}

			if(user.getGroups() != null && user.getGroups().size() > 0) {
				userRepresentation.setGroups(user.getGroups());
			}

			userRepresentation.setServiceAccountClientId(user.getServiceAccountClientId());
			userRepresentation.setSelf(user.getSelf());
			userRepresentation.setCreatedTimestamp(user.getCreatedTimestamp());
			userRepresentation.setAccess(user.getAccess());
			userRepresentation.setNotBefore(user.getNotBefore());

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
		try {
			
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

	public User getUserDetailByUsernameOrId(Logger log, String logFolder, User user) throws Exception {
		List<UserRepresentation> userRepresentations = new ArrayList<>();
		UserRepresentation userRepresentation = null;
		try {
			if(user.getId() == null || user.getId().isBlank()) {
				userRepresentations = keycloak.realm(property.getKeycloak_realm())
					    .users()
					    .search(user.getUsername(), true);
				userRepresentation = userRepresentations.get(0);
			} else {userRepresentation = keycloak.realm(property.getKeycloak_realm()).users().get(user.getId()).toRepresentation();}
			user.toBuilder()
			.id(userRepresentation.getId())
			.username(userRepresentation.getUsername())
			.enabled(userRepresentation.isEnabled())
			.emailVerified(userRepresentation.isEmailVerified())
			.email(userRepresentation.getEmail())
			.firstName(userRepresentation.getFirstName())
			.lastName(userRepresentation.getLastName())
			.attributes(userRepresentation.getAttributes())
			.disableableCredentialTypes(userRepresentation.getDisableableCredentialTypes())
			.requiredActions(userRepresentation.getRequiredActions())
			.realmRoles(userRepresentation.getRealmRoles())
			.clientRoles(userRepresentation.getClientRoles())
			.groups(userRepresentation.getGroups())
			.serviceAccountClientId(userRepresentation.getServiceAccountClientId())
			.self(userRepresentation.getSelf())
			.createdTimestamp(userRepresentation.getCreatedTimestamp())
			.access(userRepresentation.getAccess())
			.notBefore(userRepresentation.getNotBefore())
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
			throw e;
		} finally {
			try {
				
			}catch(Exception e) {}
		}
		return user;
	}
}
