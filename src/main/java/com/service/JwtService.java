package com.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.configuration.UserInfoDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	
	public String generateSecretKey() {
		SecretKey key = Jwts.SIG.HS512.key().build();  // secure default size (256-bit for HS256)
        return Encoders.BASE64.encode(key.getEncoded());
	}
	
	public String generateToken(String userName, UserInfoDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        List<String> role = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority()) // Convert GrantedAuthority to String
                .filter(authority -> authority.startsWith("ROLE_"))
                .collect(Collectors.toList());
        claims.put("role", role);
        claims.put("permission", userDetails.getUser_action_permission());
        return createToken(claims, userName, userDetails.getJwt_token_expiration(), userDetails.getJwt_token_secret_key());
    }
	
	private String createToken(Map<String, Object> claims, String userName, int jwt_token_expiration, String jwt_token_secret_key) {
        return Jwts.builder()
                .claims(claims)
                .issuer("")
                .subject(userName)
                .audience().add(userName).and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + (jwt_token_expiration * 1000)))
                .id(UUID.randomUUID().toString())//Useful for session management(e.g logout)
                .signWith(getSignKey(jwt_token_secret_key))
                .compact();
    }
	
	private SecretKey getSignKey(String jwt_token_secret_key) {
        byte[] keyBytes = Decoders.BASE64.decode(jwt_token_secret_key);
        return Keys.hmacShaKeyFor(keyBytes);
    }
	
	private Claims extractAllClaims(String token, String jwt_token_secret_key) {
        return Jwts.parser()
                .verifyWith(getSignKey(jwt_token_secret_key))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
	
	public Boolean validateToken(String token, String jwt_token_secret_key) {
		Claims claims = extractAllClaims(token, jwt_token_secret_key);
		boolean isExpire = claims.getExpiration().before(new Date());
        return !isExpire;
    }
	
	public String extractUsername(String token) {
		DecodedJWT jwt = JWT.decode(token); // No verification here
        return jwt.getClaim("sub").asString(); // "sub" is often used for username
    }
}
