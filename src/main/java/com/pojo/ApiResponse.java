package com.pojo;


import com.enums.ResponseCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.modal.onboard.Merchant;

import lombok.*;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiResponse {

	@JsonProperty(value= "resp_code", access = Access.READ_ONLY)
	private int resp_code;
	
	@JsonIgnore
	private String resp_status;
	
	@JsonProperty(value= "resp_msg", access = Access.READ_ONLY)
	private String resp_msg;
	
	@JsonProperty(value= "datetime", access = Access.READ_ONLY)
	private String datetime;
	
	@JsonProperty(value= "merchant_data", access = Access.READ_ONLY)
	private Merchant merchant;
	
	@JsonProperty(value= "oauth", access = Access.READ_ONLY)
	private OAuth oauth;

	public ApiResponse(int resp_code, String datetime) {
		super();
		this.resp_code = resp_code;
		this.resp_status = ResponseCode.getStatusByCode(resp_code);
		this.resp_msg = ResponseCode.getDescriptionByCode(resp_code);
		this.datetime = datetime;
	}

	public ApiResponse(int resp_code, String resp_msg, String datetime) {
		super();
		this.resp_code = resp_code;
		this.resp_msg = resp_msg;
		this.datetime = datetime;
	}

	public ApiResponse(int resp_code, String datetime, Merchant merchant) {
		super();
		this.resp_code = resp_code;
		this.resp_status = ResponseCode.getStatusByCode(resp_code);
		this.resp_msg = ResponseCode.getDescriptionByCode(resp_code);
		this.datetime = datetime;
		this.merchant = merchant;
	}

	public ApiResponse(int resp_code, String resp_msg, String datetime, Merchant merchant) {
		super();
		this.resp_code = resp_code;
		this.resp_msg = resp_msg;
		this.datetime = datetime;
		this.merchant = merchant;
	}

	public ApiResponse(int resp_code, String datetime, OAuth oauth) {
		super();
		this.resp_code = resp_code;
		this.datetime = datetime;
		this.oauth = oauth;
	}
}
