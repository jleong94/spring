package com.modal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity(name = "user_action_permission")
@Table(name = "user_action_permission", schema = "appdb",
indexes = {
		@Index(name = "IX_user_action_permission_user_id", columnList = "user_id"),
		@Index(name = "IX_user_action_permission_user_action_id", columnList = "user_action_id"),
		@Index(name = "IX_user_action_permission_permission_id", columnList = "permission_id"),
})
public class UserActionPermission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Adjust strategy based on your database
	@Column(name = "id", unique = true, nullable = false, insertable = true, updatable = false, table = "user_action_permission")
	@JsonIgnore
	private Long id;
	
	@JsonIgnore
	@ManyToOne(targetEntity = User.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", referencedColumnName = "id", unique = false, nullable = false)
	private User user;
	
	@JsonIgnore
	@ManyToOne(targetEntity = UserActionLookup.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JoinColumn(name = "user_action_id", referencedColumnName = "user_action_id", unique = false, nullable = false)
	private UserActionLookup userActionLookup;
	
	@JsonIgnore
	@ManyToOne(targetEntity = Permission.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JoinColumn(name = "permission_id", referencedColumnName = "permission_id", unique = false, nullable = false)
	private Permission permission;
}
