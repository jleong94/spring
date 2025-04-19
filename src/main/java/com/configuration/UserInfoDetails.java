package com.configuration;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.modal.User;

/*
 * Re-implement UserDetails will auto call by spring security or application logic
 * */
public class UserInfoDetails implements UserDetails {

	private static final long serialVersionUID = 1L;
	private String username;
    private String password;
    private List<Object> user_action_permission;
    private List<GrantedAuthority> authorities;
    
    public UserInfoDetails(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.user_action_permission = user.getUserActionLookup()
        		.stream()
        		.filter(userAction -> userAction.getPermission() != null) // Ensure permissions list is not null
        		.flatMap(userAction -> userAction.getPermission().stream()
        				.map(permission -> {
        					String actionName = userAction.getAction_name();
        					String permissionName = permission.getPermission_name();
        					return actionName + "_" + permissionName;
        				})
        				)
        		.distinct() // Remove duplicates if any
        		.collect(Collectors.toList());
        this.authorities = user.getUserActionLookup()
        		.stream()
        		.filter(userAction -> userAction.getPermission() != null) // Ensure permissions list is not null
        		.flatMap(userAction -> userAction.getPermission().stream()
        				.map(permission -> {
        					String actionName = userAction.getAction_name();
        					String permissionName = permission.getPermission_name();
        					return new SimpleGrantedAuthority(actionName + "_" + permissionName);
        				})
        				)
        		.distinct() // Remove duplicates if any
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

	public List<Object> getUser_action_permission() {
		return user_action_permission;
	}

	public void setUser_action_permission(List<Object> user_action_permission) {
		this.user_action_permission = user_action_permission;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setAuthorities(List<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}
}
