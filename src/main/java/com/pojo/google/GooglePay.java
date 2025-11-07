package com.pojo.google;

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
@Schema(description = "Google Pay request response payload")
public class GooglePay {
	
	public interface Post {}

	@JsonProperty(value= "encryptedMsg", access = Access.WRITE_ONLY)
	@JsonView({Post.class})
	@Schema(description = "Encrypted/signed Message")
	@NotBlank(groups = {Post.class}, message = "Encrypted/signed message is blank.")
	private String encryptedMsg;
	
	@JsonProperty(value= "gatewayMerchantId", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@Schema(description = "Gateway merchant id.")
	private String gatewayMerchantId;
	
	@JsonProperty(value= "messageExpiration", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@Schema(description = "Date and time at which the message expires as UTC milliseconds since epoch. Integrators should reject any message that's expired.")
	private String messageExpiration;
	
	@JsonProperty(value= "messageId", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@Schema(description = "A unique ID that identifies the message in case it needs to be revoked or located at a later time.")
	private String messageId;
	
	@JsonProperty(value= "paymentMethod", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@Schema(description = "The type of the payment credential. Currently, only CARD is supported.")
	private String paymentMethod;
	
	@JsonProperty(value= "paymentMethodDetails", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@Schema(description = "The payment credential itself. The format of this object is determined by the paymentMethod and is described in the following tables.")
	private PaymentMethodDetail paymentMethodDetail;
}
