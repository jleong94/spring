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
import jakarta.persistence.OneToMany;
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
@Entity(name = "user_status_lookup")
@Table(name = "user_status_lookup", schema = "appdb",
indexes = {
		@Index(name = "idx_status_name", columnList = "status_name")
})
public class UserStatusLookup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Adjust strategy based on your database
	@Column(name = "status_id", unique = true, nullable = false, insertable = true, updatable = false, table = "user_status_lookup")
	@JsonIgnore
	private Long status_id;

	@JsonProperty(value= "status_name", access = Access.READ_ONLY)
	@Column(name = "status_name", unique = true, nullable = false, insertable = true, updatable = true, table = "user_status_lookup", length = 20)
	private String status_name;
	
	@JsonIgnore
	@OneToMany(targetEntity = User.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER, mappedBy = "userStatusLookup")
    private List<User> user;
}
