package com.modal;

import java.util.List;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
	
	@JsonProperty(value= "username", access = Access.READ_WRITE)
	@NotNull(message = "User name is null.")
	@NotBlank(message = "User name is blank.")
	@Size(max = 20, message = "User name exceed 20 characters.")
	@Column(name = "username", length = 20, nullable = false, unique = true)
	private String username;
	
	@JsonProperty(value= "password", access = Access.READ_WRITE)
	@NotNull(message = "Password is null.")
	@NotBlank(message = "Password is blank.")
	@Size(max = 255, message = "Hashed password exceed 255 characters.")
	@Column(name = "password", length = 255, nullable = false)
	private String password;
	
	@JsonProperty(value= "email", access = Access.READ_WRITE)
	@NotNull(message = "Email is null.")
	@NotBlank(message = "Email is blank.")
	@Size(max = 50, message = "Email exceed 50 characters.")
	@Column(name = "email", length = 50, nullable = false)
	private String email;
	
	@JsonProperty(value= "user_status", access = Access.READ_ONLY)
	@ManyToOne(targetEntity = UserStatusLookup.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JoinColumn(name = "status_id", referencedColumnName = "status_id", unique = false, nullable = false)
	private UserStatusLookup userStatusLookup;
	
	@JsonProperty(value= "role", access = Access.READ_ONLY)
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
