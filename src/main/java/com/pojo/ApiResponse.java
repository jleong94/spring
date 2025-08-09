package com.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.pojo.keycloak.User;
import com.pojo.keycloak.User.Create;
import com.pojo.keycloak.User.Select;
import com.pojo.keycloak.User.Update;

import lombok.*;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiResponse {

	@JsonProperty(value= "resp_code", access = Access.READ_ONLY)
	@JsonView({Create.class, Update.class, Select.class})
	private int resp_code;

	@JsonProperty(value= "resp_msg", access = Access.READ_ONLY)
	@JsonView({Create.class, Update.class, Select.class})
	private String resp_msg;

	@JsonProperty(value= "datetime", access = Access.READ_ONLY)
	@JsonView({Create.class, Update.class, Select.class})
	private String datetime;

	@JsonProperty(value= "user", access = Access.READ_ONLY)
	@JsonView({Create.class, Update.class, Select.class})
	private User user;

	@JsonProperty(value= "jwt", access = Access.READ_ONLY)
	@JsonView({})
	private Jwt jwt;
}
