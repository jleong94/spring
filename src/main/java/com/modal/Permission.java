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
@Entity(name = "permission")
@Table(name = "permission", schema = "appdb")
public class Permission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Adjust strategy based on your database
	@Column(name = "permission_id", unique = true, nullable = false, insertable = true, updatable = false, table = "permission")
	@JsonIgnore
	private Long permission_id;
	
	@JsonIgnore
	@Column(name = "permission_name", unique = true, nullable = false, insertable = true, updatable = true, table = "permission", length = 20)
	private String permission_name;
	
	@JsonIgnore
	@ManyToMany(targetEntity = UserActionLookup.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER, mappedBy = "permission")
    private List<UserActionLookup> userActionLookup;
}
