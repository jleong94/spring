package com.configuration;

import java.util.Collections;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class CustomAbstractAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 1L;
	
	private final String signature;
	
	private final Object principal;

    public CustomAbstractAuthenticationToken(String signature, Object principal, boolean authenticated) {
        super(authenticated ? Collections.singletonList(new SimpleGrantedAuthority("")) : null);
        this.signature = signature;
        this.principal = principal;
        setAuthenticated(authenticated);
    }

    @Override
    public Object getCredentials() {
        return signature;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
