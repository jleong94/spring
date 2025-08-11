package com.pojo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.validation.JwtValidator;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JwtValidator
@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Jwt {
	
	@NotBlank(groups = {}, message = "Grant type is blank.")
	@JsonProperty(value= "grant_type", access = Access.WRITE_ONLY)
	private String grant_type;

	@JsonProperty(value= "username", access = Access.WRITE_ONLY)
	private String username;

	@JsonProperty(value= "password", access = Access.WRITE_ONLY)
	private String password;

	@JsonProperty(value= "access_token", access = Access.READ_ONLY)
	@JsonAlias({"user_name", "user"})
	private String access_token;

	@JsonProperty(value= "expires_in", access = Access.READ_ONLY)
	private long expires_in;

	@JsonProperty(value= "refresh_token", access = Access.READ_WRITE)
	private String refresh_token;

	@JsonProperty(value= "refresh_expires_in", access = Access.READ_ONLY)
	private long refresh_expires_in;

	@JsonProperty(value= "scope", access = Access.READ_ONLY)
	private String scope;
}
