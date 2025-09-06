package com.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.pojo.template.Pojo;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.*;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "API response payload")
public class ApiResponse {

	@JsonProperty(value= "resp_code", access = Access.READ_ONLY)
	@JsonView({Pojo.Post.class, Pojo.Get.class, Pojo.Put.class, Pojo.Delete.class})
	@Schema(description = "Response code")
	private int resp_code;

	@JsonProperty(value= "resp_msg", access = Access.READ_ONLY)
	@JsonView({Pojo.Post.class, Pojo.Get.class, Pojo.Put.class, Pojo.Delete.class})
	@Schema(description = "Response description")
	private String resp_msg;

	@JsonProperty(value= "datetime", access = Access.READ_ONLY)
	@JsonView({Pojo.Post.class, Pojo.Get.class, Pojo.Put.class, Pojo.Delete.class})
	@Schema(description = "Response datetime")
	private String datetime;
	
	@JsonProperty(value= "pojo", access = Access.READ_ONLY)
	@JsonView({Pojo.Post.class, Pojo.Get.class, Pojo.Put.class, Pojo.Delete.class})
	@Schema(description = "Sample object")
	private Pojo pojo;
}
