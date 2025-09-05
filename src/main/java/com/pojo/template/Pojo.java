package com.pojo.template;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

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
public class Pojo {

	public class Post {}
	public class Get {}
	public class Put {}
	public class Delete {}
	
	@Positive(message = "Id must larger than 0")
	@JsonProperty(value= "id", access = Access.READ_WRITE)
	@JsonView({Post.class, Get.class, Put.class})
	private int id;
	
	@NotBlank(groups = {Post.class, Put.class}, message = "Name is blank.")
	@JsonProperty(value= "name", access = Access.READ_WRITE)
	@JsonView({Post.class, Get.class, Put.class})
	private String name;
	
	@NotBlank(groups = {Post.class, Put.class}, message = "IC is blank.")
	@JsonProperty(value= "ic", access = Access.READ_WRITE)
	@JsonView({Post.class, Get.class, Put.class})
	private String ic;
	
	@JsonProperty(value= "dateOfBirth", access = Access.READ_WRITE)
	@JsonView({Post.class, Get.class, Put.class})
	private String dateOfBirth;
	
	@JsonProperty(value= "password", access = Access.READ_WRITE)
	@JsonView({Get.class})
	private String password;
	
	@DecimalMin(groups = {Post.class, Put.class}, value = "0.00", inclusive = true, message = "Account balance must be larger or equal to 0.00")
	@JsonProperty(value= "account_balance", access = Access.READ_WRITE)
	@JsonView({Post.class, Get.class, Put.class})
	private BigDecimal account_balance;
}
