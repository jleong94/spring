package com.service.keycloak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

import com.exception.DuplicatedEmailException;
import com.exception.DuplicatedUsernameException;
import com.exception.UserNotFoundException;
import com.pojo.Property;
import com.pojo.keycloak.Access;
import com.pojo.keycloak.Credential;
import com.pojo.keycloak.FederatedIdentitie;
import com.pojo.keycloak.User;

import jakarta.ws.rs.NotFoundException;
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
