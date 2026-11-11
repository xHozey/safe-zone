package com.api.gateway.utils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private final Key secretKey = Keys.hmacShaKeyFor("buy01-user-service-jwt-secret-key-change-me-2026".getBytes(StandardCharsets.UTF_8));

    // public JwtUtil(@Value("buy01-user-service-jwt-secret-key-change-me-2026") String secret) {
    //     this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    // }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}