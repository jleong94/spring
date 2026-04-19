package com.pojo.google;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

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
public class GooglePay {
	
	public interface Post {}

	@JsonProperty(value= "encryptedMsg", access = Access.READ_WRITE)
	@JsonView({Post.class})
	@NotBlank(groups = {Post.class}, message = "Encrypted/signed message is blank.")
	private String encryptedMsg;
	
	@JsonProperty(value= "gatewayMerchantId", access = Access.READ_WRITE)
	@JsonView({Post.class})
	private String gatewayMerchantId;
	
	@JsonProperty(value= "messageExpiration", access = Access.READ_WRITE)
	@JsonView({Post.class})
	private String messageExpiration;
	
	@JsonProperty(value= "messageId", access = Access.READ_WRITE)
	@JsonView({Post.class})
	private String messageId;
	
	@JsonProperty(value= "paymentMethod", access = Access.READ_WRITE)
	@JsonView({Post.class})
	private String paymentMethod;
	
	@JsonProperty(value= "paymentMethodDetails", access = Access.READ_WRITE)
	@JsonView({Post.class})
	private PaymentMethodDetail paymentMethodDetail;
}
