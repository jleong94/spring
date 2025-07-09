package com.configuration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

@Component
//Converts Keycloak roles from a JWT into Spring Security GrantedAuthority objects.
public class KeycloakConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

	/**
	 * Extracts roles from the "realm_access" claim in the JWT and converts them to GrantedAuthority.
	 * 
	 * @param jwt the JWT containing user information and claims
	 * @return a collection of GrantedAuthority objects based on Keycloak roles
	 */
	@Override
	public Collection<GrantedAuthority> convert(Jwt jwt) {
		// Extract the "realm_access" claim from the JWT
		Map<String, Object> realmAccess = jwt.getClaim("realm_access");
		// If the claim is missing or doesn't contain "roles", return an empty list
		if (realmAccess == null || !realmAccess.containsKey("roles")) return List.of();
		// Get the "roles" object from the realmAccess map
		Object rolesObj = realmAccess.get("roles");
		// Ensure the roles object is a collection; otherwise, return empty list
		if (!(rolesObj instanceof Collection<?> roles)) return List.of();
		// Convert each role string to a SimpleGrantedAuthority with the prefix "ROLE_"
		return roles.stream()
				.filter(String.class::isInstance) // Filter to ensure only String elements are processed
				.map(String.class::cast)// Cast Object to String
				.map(role -> "ROLE_" + role.toUpperCase())// Format to Spring Security convention
				.map(SimpleGrantedAuthority::new)// Wrap in a GrantedAuthority object
				.collect(Collectors.toSet()); // Collect into a Set to avoid duplicates
	}

}
