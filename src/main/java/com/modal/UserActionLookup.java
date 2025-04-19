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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity(name = "user_action_lookup")
@Table(name = "user_action_lookup", schema = "appdb")
public class UserActionLookup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Adjust strategy based on your database
	@Column(name = "user_action_id", nullable = false)
	@JsonIgnore
	private Long user_action_id;
	
	@JsonIgnore
	@Column(name = "action_name", nullable = false, length = 255, unique = true)
	private String action_name;
	
	@JsonIgnore
	@ManyToMany(targetEntity = User.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER, mappedBy = "userActionLookup")
    private List<User> user;
	
	@JsonProperty(value= "permission", access = Access.READ_ONLY)
	@ManyToMany(targetEntity = Permission.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JoinTable(name = "user_action_permission", 
	joinColumns = {@JoinColumn(name = "user_action_id", referencedColumnName = "user_action_id", unique = false, nullable = false)},
	inverseJoinColumns = {@JoinColumn(name = "permission_id", referencedColumnName = "permission_id", unique = false, nullable = false)})
	private List<Permission> permission;
}
