package com.pojo.keycloak;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "Auth request payload")
public class AuthRequest {

	public interface Post {}

	@JsonView({Post.class})
	@NotBlank(groups = {Post.class}, message = "Username is blank")
	@JsonProperty(value= "username", access = Access.READ_WRITE)
	@Schema(description = "Username", example = "admin@company.com")
	private String username;

	@JsonView({Post.class})
	@NotBlank(groups = {Post.class}, message = "Password is blank")
	@JsonProperty(value= "password", access = Access.READ_WRITE)
	@Schema(description = "Password", example = "securePassword123")
	private String password;
}
