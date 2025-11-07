package com.pojo.google;

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
@Schema(description = "Google Pay(PaymentMethodDetail) request response payload")
public class PaymentMethodDetail {
	
	public interface Post {}

	@JsonProperty(value= "pan", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@Schema(description = "The personal account number charged. This string contains only digits.")
	private String pan;

	@JsonProperty(value= "expirationMonth", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@Schema(description = "The expiration month of the card, where 1 represents January, 2 represents February, and so on.")
	private int expirationMonth;

	@JsonProperty(value= "expirationYear", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@Schema(description = "The four-digit expiration year of the card, such as 2020.")
	private int expirationYear;

	@JsonProperty(value= "authMethod", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@Schema(description = "The authentication method of the card transaction.")
	private String authMethod;

	@JsonProperty(value= "cryptogram", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@Schema(description = "A 3-D Secure cryptogram.")
	private String cryptogram;

	@JsonProperty(value= "eciIndicator", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@Schema(description = "This string isn’t always present. It returns only for authenticated device tokens transactions on Android (CRYPTOGRAM_3DS). This value must be passed down the payment processing flow.")
	private String eciIndicator;
}
