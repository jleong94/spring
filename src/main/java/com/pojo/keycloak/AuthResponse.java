package com.pojo.keycloak;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "Auth response payload")
public class AuthResponse {

	public interface Post {}

	@JsonView({Post.class})
	@JsonProperty(value= "access_token", access = Access.READ_WRITE)
	@Schema(description = "JWT access token")
	private String access_token;

	@JsonView({Post.class})
	@JsonProperty(value= "refresh_token", access = Access.READ_WRITE)
	@Schema(description = "JWT refresh token")
	private String refresh_token;

	@JsonView({Post.class})
	@JsonProperty(value= "token_type", access = Access.READ_WRITE)
	@Schema(description = "Token type", example = "Bearer")
	private String token_type;

	@JsonView({Post.class})
	@JsonProperty(value= "expires_in", access = Access.READ_WRITE)
	@Schema(description = "Token expiration time in seconds")
	private int expires_in;

	@JsonView({Post.class})
	@JsonProperty(value= "refresh_expires_in", access = Access.READ_WRITE)
	@Schema(description = "Refresh token expiration time in seconds")
	private int refresh_expires_in;

	@JsonView({Post.class})
	@JsonProperty(value= "username", access = Access.READ_WRITE)
	@Schema(description = "Username", example = "john.smith")
	private String username;

	@JsonView({Post.class})
	@JsonProperty(value= "roles", access = Access.READ_WRITE)
	@Schema(description = "User roles")
	private Set<String> roles;
}
