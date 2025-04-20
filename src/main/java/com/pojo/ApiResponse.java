package com.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.modal.EMail;

import lombok.*;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiResponse {

	@JsonProperty(value= "resp_code", access = Access.READ_ONLY)
	private int resp_code;
	
	@JsonProperty(value= "resp_msg", access = Access.READ_ONLY)
	private String resp_msg;
	
	@JsonProperty(value= "datetime", access = Access.READ_ONLY)
	private String datetime;
	
	@JsonProperty(value= "oauth", access = Access.READ_ONLY)
	private OAuth oauth;
	
	@JsonProperty(value= "email", access = Access.READ_ONLY)
	private EMail email;

	public ApiResponse(int resp_code, String resp_msg, String datetime) {
		super();
		this.resp_code = resp_code;
		this.resp_msg = resp_msg;
		this.datetime = datetime;
	}

	public ApiResponse(int resp_code, String datetime, OAuth oauth) {
		super();
		this.resp_code = resp_code;
		this.datetime = datetime;
		this.oauth = oauth;
	}

	public ApiResponse(int resp_code, String datetime, EMail email) {
		super();
		this.resp_code = resp_code;
		this.datetime = datetime;
		this.email = email;
	}
}
