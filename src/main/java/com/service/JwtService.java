package com.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import com.configuration.UserInfoDetails;
import com.properties.Property;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	@Autowired
	Property property;
	
	public String generateToken(String userName, @AuthenticationPrincipal UserInfoDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        List<String> role = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority()) // Convert GrantedAuthority to String
                .filter(authority -> authority.startsWith("ROLE_"))
                .collect(Collectors.toList());
        claims.put("role", role);
        claims.put("permission", userDetails.getUser_action_permission());
        return createToken(claims, userName);
    }
	
	private String createToken(Map<String, Object> claims, String userName) {
        return Jwts.builder()
                .claims(claims)
                .subject(userName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + (property.getJwt_token_expiration() * 1000)))
                .signWith(getSignKey())
                .compact();
    }
	
	private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(property.getJwt_secret_key());
        return Keys.hmacShaKeyFor(keyBytes);
    }
	
	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
	
	private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
	
	public Boolean validateToken(String token) {
		boolean isExpire = extractClaim(token, Claims -> Claims.getExpiration()).before(new Date());
		String username = extractClaim(token, Claims -> Claims.getSubject());
        return (username != null && !isExpire);
    }
	
	public String extractUsername(String token) {
        return extractClaim(token, Claims -> Claims.getSubject());
    }
}
