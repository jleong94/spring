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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
		@Index(name = "idx_username", columnList = "username")
})
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Adjust strategy based on your database
	@Column(name = "id", nullable = false)
	@JsonIgnore
	private Long id;
	
	@CreationTimestamp
    @Column(name = "created_datetime", nullable = false, updatable = false)
	@JsonProperty(value= "created_datetime", access = Access.READ_ONLY)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private LocalDateTime created_datetime;
	
	@UpdateTimestamp
	@Column(name = "modified_datetime")
	@JsonProperty(value= "modified_datetime", access = Access.READ_ONLY)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private LocalDateTime modified_datetime;
	
	@JsonProperty(value= "username", access = Access.READ_WRITE)
	@NotNull(message = "User name is null.")
	@NotBlank(message = "User name is blank.")
	@Size(max = 20, message = "User name exceed 20 characters.")
	@Column(name = "username", length = 20, nullable = false, unique = true)
	private String username;
	
	@JsonProperty(value= "password", access = Access.WRITE_ONLY)
	@NotNull(message = "Password is null.")
	@NotBlank(message = "Password is blank.")
	@Size(max = 255, message = "Password exceed 255 characters.")
	@Pattern(
			regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=[\\]{};':\"\\\\|,.<>/?]).{8,}$",
			message = "Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one digit, and one special character."
			)
	@Column(name = "password", length = 255, nullable = false)
	private String password;
	
	@Email(message = "Invalid email format.")
	@JsonProperty(value= "email", access = Access.READ_WRITE)
	@NotNull(message = "Email is null.")
	@NotBlank(message = "Email is blank.")
	@Size(max = 50, message = "Email exceed 50 characters.")
	@Column(name = "email", length = 50, nullable = false)
	private String email;
	
	@JsonIgnore
	@Min(1)
	@Column(name = "jwt_token_expiration", length = 10, nullable = false)
	private int jwt_token_expiration;
	
	@JsonIgnore
	@NotNull(message = "JWT token secret key is null.")
	@NotBlank(message = "JWT token secret key is blank.")
	@Size(max = 100, message = "JWT token secret key exceed 100 characters.")
	@Column(name = "jwt_token_secret_key", length = 100, nullable = false)
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
}
