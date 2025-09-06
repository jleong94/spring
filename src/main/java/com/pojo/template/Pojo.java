package com.pojo.template;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

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
@Schema(description = "Pojo request & response payload")
public class Pojo {

	public class Post {}
	public class Get {}
	public class Put {}
	public class Delete {}
	
	@Positive(message = "Id must larger than 0")
	@JsonProperty(value= "id", access = Access.READ_WRITE)
	@JsonView({Post.class, Get.class, Put.class})
	@Schema(description = "Unique identifier")
	private int id;
	
	@NotBlank(groups = {Post.class, Put.class}, message = "Name is blank.")
	@JsonProperty(value= "name", access = Access.READ_WRITE)
	@JsonView({Post.class, Get.class, Put.class})
	@Schema(description = "Name")
	private String name;
	
	@NotBlank(groups = {Post.class, Put.class}, message = "IC is blank.")
	@JsonProperty(value= "ic", access = Access.READ_WRITE)
	@JsonView({Post.class, Get.class, Put.class})
	@Schema(description = "Identity card")
	private String ic;
	
	@Pattern(
            regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])\\s([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$",
            message = "Date of birth must be in format yyyy-MM-dd HH:mm:ss"
    )
	@JsonProperty(value= "dateOfBirth", access = Access.READ_WRITE)
	@JsonView({Post.class, Get.class, Put.class})
	@Schema(description = "Date of birth")
	private String dateOfBirth;
	
	@Size(min = 8, max = 12, message = "Password must be between 8 and 12 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,12}$",
            message = "Password must contain at least one uppercase, one lowercase, one number, and one special character"
    )
	@JsonProperty(value= "password", access = Access.READ_WRITE)
	@JsonView({Get.class})
	@Schema(description = "Password")
	private String password;
	
	@Digits(integer = 7, fraction = 2, message = "Only up to max 7 digits with 2 decimal places")
	@DecimalMin(groups = {Post.class, Put.class}, value = "0.00", inclusive = true, message = "Account balance must be larger or equal to 0.00")
	@JsonProperty(value= "account_balance", access = Access.READ_WRITE)
	@JsonView({Post.class, Get.class, Put.class})
	@Schema(description = "Account balance")
	private BigDecimal account_balance;
}
