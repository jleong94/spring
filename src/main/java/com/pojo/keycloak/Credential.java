package com.pojo.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
public class Credential {

	@NotBlank(message = "Credential type is blank.")
	@JsonProperty(value= "type", access = Access.READ_WRITE)
	@JsonView({User.Create.class, User.Update.class, User.Select.class})
	private String type; //fixed value, password
	
	@NotBlank(message = "Credential is blank.")
	@JsonProperty(value= "value", access = Access.READ_WRITE)
	@JsonView({User.Create.class, User.Update.class, User.Select.class})
	private String value;
	
	@JsonProperty(value= "temporary", access = Access.READ_WRITE)
	@JsonView({User.Create.class, User.Update.class, User.Select.class})
	private boolean temporary;
}
