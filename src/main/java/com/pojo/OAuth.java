package com.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OAuth {

	@JsonProperty(value= "username", access = Access.WRITE_ONLY)
	@NotNull(message = "Username is null.")
    @NotBlank(message = "Username is blank.")
	private String username;

	@JsonProperty(value= "password", access = Access.WRITE_ONLY)
	@NotNull(message = "Password is null.")
    @NotBlank(message = "Password is blank.")
	private String password;
	
	@JsonProperty(value= "expires_in", access = Access.READ_ONLY)
	private int expires_in;
	
	@JsonProperty(value= "token_type", access = Access.READ_ONLY)
	private String token_type;
	
	@JsonProperty(value= "access_token", access = Access.READ_ONLY)
	private String access_token;
}
