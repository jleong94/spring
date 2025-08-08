package com.pojo.keycloak;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.validation.keycloak.CredentialTypeValidator;
import com.validation.keycloak.RequiredActionValidator;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class User {
	
	public interface Create {}
	public interface Update {}
	public interface Select {}

	@JsonIgnore
	private String id;
	
	@NotBlank(groups = {Create.class, Update.class}, message = "Username is blank.")
	@JsonProperty(value= "username", access = Access.READ_WRITE)
	@JsonView({Create.class, Update.class, Select.class})
	private String username;
	
	@JsonProperty(value= "enabled", access = Access.READ_ONLY)
	@JsonView({Create.class, Update.class, Select.class})
	private boolean enabled;
	
	@JsonProperty(value= "emailVerified", access = Access.READ_ONLY)
	@JsonView({Create.class, Update.class, Select.class})
	private boolean emailVerified;
	
	@NotBlank(groups = {Create.class, Update.class}, message = "Email is blank.")
	@Size(groups = {Create.class, Update.class}, max = 255, message = "Email exceed 255 characters.")
	@JsonProperty(value= "email", access = Access.READ_WRITE)
	@JsonView({Create.class, Update.class, Select.class})
	private String email;
	
	@NotBlank(groups = {Create.class, Update.class}, message = "First name is blank.")
	@Size(groups = {Create.class, Update.class}, max = 255, message = "First name exceed 255 characters.")
	@JsonProperty(value= "firstName", access = Access.READ_WRITE)
	@JsonView({Create.class, Update.class, Select.class})
	private String firstName;
	
	@NotBlank(groups = {Create.class, Update.class}, message = "Last name is blank.")
	@Size(groups = {Create.class, Update.class}, max = 255, message = "Last name exceed 255 characters.")
	@JsonProperty(value= "lastName", access = Access.READ_WRITE)
	@JsonView({Create.class, Update.class, Select.class})
	private String lastName;
	
	@JsonProperty(value= "attributes", access = Access.READ_WRITE)
	@JsonView({Create.class, Update.class, Select.class})
	private Map<String, List<String>> attributes;
	
	@JsonProperty(value= "credentials", access = Access.READ_ONLY)
	@JsonView({Create.class, Update.class, Select.class})
	private List<Credential> credentials;
	
	@CredentialTypeValidator(groups = {Create.class, Update.class})
	@JsonProperty(value= "disableableCredentialTypes", access = Access.READ_ONLY)
	@JsonView({Create.class, Update.class, Select.class})
	private Set<String> disableableCredentialTypes; // password, otp, webauthn, webauthn-passwordless, authenticator
	
	@RequiredActionValidator(groups = {Create.class, Update.class})
	@JsonProperty(value= "requiredActions", access = Access.READ_ONLY)
	@JsonView({Create.class, Update.class, Select.class})
	private List<String> requiredActions; // VERIFY_EMAIL, UPDATE_PASSWORD, UPDATE_PROFILE, CONFIGURE_TOTP, TERMS_AND_CONDITIONS, WEBAUTHN_REGISTER, WEBAUTHN_REGISTER_PASSWORDLESS
	
	@JsonProperty(value= "federatedIdentities", access = Access.READ_WRITE)
	@JsonView({Create.class, Update.class, Select.class})
	private List<FederatedIdentitie> federatedIdentities;
	
	@JsonProperty(value= "realmRoles", access = Access.READ_ONLY)
	@JsonView({Create.class, Update.class, Select.class})
	private List<String> realmRoles;
	
	@JsonProperty(value= "clientRoles", access = Access.READ_ONLY)
	@JsonView({Create.class, Update.class, Select.class})
	private Map<String, List<String>> clientRoles;
	
	@JsonProperty(value= "groups", access = Access.READ_ONLY)
	@JsonView({Create.class, Update.class, Select.class})
	private List<String> groups;
	
	@JsonProperty(value= "serviceAccountClientId", access = Access.READ_ONLY)
	@JsonView({Create.class, Update.class, Select.class})
	private String serviceAccountClientId;
	
	@JsonProperty(value= "self", access = Access.READ_ONLY)
	@JsonView({Create.class, Update.class, Select.class})
	private String self;
	
	@JsonProperty(value= "createdTimestamp", access = Access.READ_ONLY)
	@JsonView({Create.class, Update.class, Select.class})
	private long createdTimestamp;
	
	@JsonProperty(value= "access", access = Access.READ_ONLY)
	@JsonView({Create.class, Update.class, Select.class})
	private com.pojo.keycloak.Access access;
	
	@JsonProperty(value= "notBefore", access = Access.READ_ONLY)
	@JsonView({Create.class, Update.class, Select.class})
	private int notBefore;
}
