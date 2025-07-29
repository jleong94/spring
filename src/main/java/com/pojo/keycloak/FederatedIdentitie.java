package com.pojo.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
public class FederatedIdentitie {

	@Size(groups = {User.Create.class, User.Update.class}, max = 255, message = "Identity provider exceed 255 characters.")
	@JsonProperty(value= "identityProvider", access = Access.READ_WRITE)
	@JsonView({User.Create.class, User.Update.class, User.Select.class})
	private String identityProvider;
	
	@Size(groups = {User.Create.class, User.Update.class}, max = 255, message = "User id exceed 255 characters.")
	@JsonProperty(value= "userId", access = Access.READ_WRITE)
	@JsonView({User.Create.class, User.Update.class, User.Select.class})
	private String userId;
	
	@Size(groups = {User.Create.class, User.Update.class}, max = 255, message = "User name exceed 255 characters.")
	@JsonProperty(value= "userName", access = Access.READ_WRITE)
	@JsonView({User.Create.class, User.Update.class, User.Select.class})
	private String userName;
}
