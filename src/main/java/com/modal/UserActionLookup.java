package com.modal;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Entity(name = "user_action_lookup")
@Table(name = "user_action_lookup", schema = "appdb")
public class UserActionLookup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Adjust strategy based on your database
	@Column(name = "user_action_id", unique = true, nullable = false, insertable = true, updatable = false, table = "user_action_lookup")
	@JsonIgnore
	private Long user_action_id;
	
	@JsonIgnore
	@Column(name = "action_name", unique = true, nullable = false, insertable = true, updatable = true, table = "user_action_lookup", length = 255)
	private String action_name;
	
	@JsonIgnore
	@OneToMany(targetEntity = UserActionPermission.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER, mappedBy = "userActionLookup")
    private List<UserActionPermission> userActionPermission;
}
