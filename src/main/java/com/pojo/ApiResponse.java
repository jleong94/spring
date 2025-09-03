package com.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.pojo.template.Pojo;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.*;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiResponse {

	@JsonProperty(value= "resp_code", access = Access.READ_ONLY)
	@JsonView({Pojo.PojoPost.class, Pojo.PojoGet.class, Pojo.PojoPut.class, Pojo.PojoDelete.class})
	private int resp_code;

	@JsonProperty(value= "resp_msg", access = Access.READ_ONLY)
	@JsonView({Pojo.PojoPost.class, Pojo.PojoGet.class, Pojo.PojoPut.class, Pojo.PojoDelete.class})
	private String resp_msg;

	@JsonProperty(value= "datetime", access = Access.READ_ONLY)
	@JsonView({Pojo.PojoPost.class, Pojo.PojoGet.class, Pojo.PojoPut.class, Pojo.PojoDelete.class})
	private String datetime;
	
	@JsonProperty(value= "pojo", access = Access.READ_ONLY)
	@JsonView({Pojo.PojoPost.class, Pojo.PojoGet.class, Pojo.PojoPut.class, Pojo.PojoDelete.class})
	private Pojo pojo;
}
