package com.pojo.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
public class Access {

	@JsonProperty(value= "manageGroupMembership", access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
	@JsonView({User.Create.class, User.Update.class, User.Select.class})
	private boolean manageGroupMembership;
	
	@JsonProperty(value= "view", access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
	@JsonView({User.Create.class, User.Update.class, User.Select.class})
	private boolean view;
	
	@JsonProperty(value= "mapRoles", access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
	@JsonView({User.Create.class, User.Update.class, User.Select.class})
	private boolean mapRoles;
	
	@JsonProperty(value= "impersonate", access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
	@JsonView({User.Create.class, User.Update.class, User.Select.class})
	private boolean impersonate;
	
	@JsonProperty(value= "manage", access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
	@JsonView({User.Create.class, User.Update.class, User.Select.class})
	private boolean manage;
}
