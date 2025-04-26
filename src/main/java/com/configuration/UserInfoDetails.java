package com.configuration;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.modal.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Re-implement UserDetails will auto call by spring authentication/authorization or application logic
 * */
@Data//Shortcut for @ToString, @EqualsAndHashCode, @Getter on all fields, and @Setter on all non-final fields, and @RequiredArgsConstructor
@AllArgsConstructor//Generates a constructor with parameters for all fields (regardless of type or annotations)
@NoArgsConstructor//Generates a constructor with no parameters
@Builder(toBuilder = true)
public class UserInfoDetails implements UserDetails {

	private static final long serialVersionUID = 1L;
	private String username;
    private String password;
    private List<Object> user_action_permission;
    private List<GrantedAuthority> authorities;
    private int jwt_token_expiration;
    private String jwt_token_secret_key;
    
    public UserInfoDetails(User user) {
    	this.username = user.getUsername();
    	this.password = user.getPassword();
    	this.jwt_token_expiration = user.getJwt_token_expiration();
    	this.jwt_token_secret_key = user.getJwt_token_secret_key();
    	this.user_action_permission = user.getUserActionPermission()
    			.stream()
    			.map(uap -> uap.getUserActionLookup().getAction_name() + "_" + uap.getPermission().getPermission_name())
    			.collect(Collectors.toList());
    	this.authorities = user.getUserActionPermission()
    			.stream()
    			.map(uap -> new SimpleGrantedAuthority(
    					uap.getUserActionLookup().getAction_name() + "_" + uap.getPermission().getPermission_name()))
    			.collect(Collectors.toList());
    	this.authorities.add(new SimpleGrantedAuthority("ROLE_".concat(user.getUserRoleLookup().getRole_name())));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Implement your logic if you need this
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Implement your logic if you need this
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Implement your logic if you need this
    }

    @Override
    public boolean isEnabled() {
        return true; // Implement your logic if you need this
    }
}
