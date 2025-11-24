package com.configuration;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Converts Keycloak JWT tokens to Spring Security Authentication with roles and permissions
 */
@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
		String principalClaimValue = jwt.getClaimAsString("preferred_username");
		if (principalClaimValue == null) {
			principalClaimValue = jwt.getSubject();
		}
		return new JwtAuthenticationToken(jwt, authorities, principalClaimValue);
	}

	/**
	 * Extract authorities from JWT including:
	 * - Realm roles as ROLE_<role>
	 * - Client roles as ROLE_<client>_<role>
	 * - Custom permissions as PERMISSION_<resource>_<action>
	 */
	private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
		Set<GrantedAuthority> authorities = new HashSet<>();

		// Extract realm roles
		Map<String, Object> realmAccess = jwt.getClaim("realm_access");
		if (realmAccess != null && realmAccess.get("roles") != null) {
			@SuppressWarnings("unchecked")
			List<String> realmRoles = (List<String>) realmAccess.get("roles");
			authorities.addAll(realmRoles.stream()
					.map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
					.collect(Collectors.toSet()));
		}

		// Extract resource (client) roles
		Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
		if (resourceAccess != null) {
			resourceAccess.forEach((resource, resourceClaims) -> {
				@SuppressWarnings("unchecked")
				Map<String, Object> resourceClaimsMap = (Map<String, Object>) resourceClaims;
				if (resourceClaimsMap.get("roles") != null) {
					@SuppressWarnings("unchecked")
					List<String> resourceRoles = (List<String>) resourceClaimsMap.get("roles");
					authorities.addAll(resourceRoles.stream()
							.map(role -> new SimpleGrantedAuthority("ROLE_" + resource.toUpperCase() + "_" + role.toUpperCase()))
							.collect(Collectors.toSet()));
				}
			});
		}

		return authorities;
	}
}
