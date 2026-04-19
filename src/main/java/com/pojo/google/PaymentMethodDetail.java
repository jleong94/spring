package com.pojo.google;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PaymentMethodDetail {
	
	public interface Post {}

	@JsonProperty(value= "pan", access = Access.READ_WRITE)
	@JsonView({Post.class})
	private String pan;

	@JsonProperty(value= "expirationMonth", access = Access.READ_WRITE)
	@JsonView({Post.class})
	private int expirationMonth;

	@JsonProperty(value= "expirationYear", access = Access.READ_WRITE)
	@JsonView({Post.class})
	private int expirationYear;

	@JsonProperty(value= "authMethod", access = Access.READ_WRITE)
	@JsonView({Post.class})
	private String authMethod;

	@JsonProperty(value= "cryptogram", access = Access.READ_WRITE)
	@JsonView({Post.class})
	private String cryptogram;

	@JsonProperty(value= "eciIndicator", access = Access.READ_WRITE)
	@JsonView({Post.class})
	private String eciIndicator;
}
