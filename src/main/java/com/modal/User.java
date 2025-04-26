package com.modal;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonView;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity(name = "user")
@Table(name = "user", schema = "appdb",
indexes = {
		@Index(name = "IX_user_username", columnList = "username"),
		@Index(name = "IX_user_username_email", columnList = "username, email")
})
public class User {
	
	public interface UserRegistration {}
    public interface ResetPassword {}
    public interface OauthToken {}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Adjust strategy based on your database
	@Column(name = "id", unique = true, nullable = false, insertable = true, updatable = false, table = "user")
	@JsonIgnore
	private Long id;
	
	@CreationTimestamp
    @Column(name = "created_datetime", unique = false, nullable = false, insertable = true, updatable = false, table = "user")
	@JsonProperty(value= "created_datetime", access = Access.READ_ONLY)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	@JsonView({UserRegistration.class})
	private LocalDateTime created_datetime;
	
	@UpdateTimestamp
	@Column(name = "modified_datetime", unique = false, nullable = false, insertable = true, updatable = true, table = "user")
	@JsonProperty(value= "modified_datetime", access = Access.READ_ONLY)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	@JsonView({UserRegistration.class})
	private LocalDateTime modified_datetime;
	
	@JsonProperty(value= "username", access = Access.READ_WRITE)
	@NotBlank(groups = {UserRegistration.class, ResetPassword.class, OauthToken.class}, message = "User name is blank.")
	@Size(groups = {UserRegistration.class, ResetPassword.class, OauthToken.class}, max = 20, message = "User name exceed 20 characters.")
	@Column(name = "username", unique = true, nullable = false, insertable = true, updatable = false, table = "user", length = 20)
	@JsonView({UserRegistration.class})
	private String username;
	
	@JsonProperty(value= "password", access = Access.WRITE_ONLY)
	@NotBlank(groups = {UserRegistration.class, OauthToken.class}, message = "Password is blank.")
	@Size(groups = {UserRegistration.class, OauthToken.class}, max = 255, message = "Password exceed 255 characters.")
	@Pattern(
			regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=[\\]{};':\"\\\\|,.<>/?]).{8,}$",
			message = "Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one digit, and one special character."
			)
	@Column(name = "password", unique = false, nullable = false, insertable = true, updatable = true, table = "user", length = 255)
	private String password;
	
	@Email(groups = {UserRegistration.class, ResetPassword.class}, message = "Invalid email format.")
	@JsonProperty(value= "email", access = Access.READ_WRITE)
	@NotBlank(groups = {UserRegistration.class, ResetPassword.class}, message = "Email is blank.")
	@Size(groups = {UserRegistration.class, ResetPassword.class}, max = 50, message = "Email exceed 50 characters.")
	@Column(name = "email", unique = true, nullable = false, insertable = true, updatable = true, table = "user", length = 50)
	@JsonView({UserRegistration.class})
	private String email;
	
	@Pattern(
			groups = {UserRegistration.class},
			regexp = "^\\+?[1-9]\\d{1,14}$",
			message = "Phone number should be in the format +<country code><number>, no spaces or dashes."
			)
	@JsonProperty(value= "mobile_no", access = Access.READ_WRITE)
	@NotBlank(groups = {UserRegistration.class}, message = "Mobile number is blank.")
	@Column(name = "mobile_no", unique = false, nullable = false, insertable = true, updatable = true, table = "user", length = 15)
	@JsonView({UserRegistration.class})
	private String mobile_no;
	
	@JsonIgnore
	@Column(name = "jwt_token_expiration", unique = false, nullable = false, insertable = true, updatable = true, table = "user")
	private int jwt_token_expiration;
	
	@JsonIgnore
	@Column(name = "jwt_token_secret_key", unique = false, nullable = false, insertable = true, updatable = true, table = "user", length = 100)
	private String jwt_token_secret_key;
	
	@JsonIgnore
	@ManyToOne(targetEntity = UserStatusLookup.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JoinColumn(name = "status_id", referencedColumnName = "status_id", unique = false, nullable = false)
	private UserStatusLookup userStatusLookup;
	
	@JsonIgnore
	@ManyToOne(targetEntity = UserRoleLookup.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id", referencedColumnName = "role_id", unique = false, nullable = false)
	private UserRoleLookup userRoleLookup;
	
	@JsonIgnore
	@ManyToMany(targetEntity = UserActionLookup.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JoinTable(name = "user_action", 
	joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id", unique = false, nullable = false)},
	inverseJoinColumns = {@JoinColumn(name = "user_action_id", referencedColumnName = "user_action_id", unique = false, nullable = false)})
	private List<UserActionLookup> userActionLookup;
	
	@Transient
	@JsonProperty(value= "token_type", access = Access.READ_ONLY)
	@JsonView({OauthToken.class})
	private String token_type;
	
	@Transient
	@JsonProperty(value= "access_token", access = Access.READ_ONLY)
	@JsonView({OauthToken.class})
	private String access_token;
}
