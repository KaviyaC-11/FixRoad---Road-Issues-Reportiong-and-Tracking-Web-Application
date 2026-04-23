package com.fixroad.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    // ==================== SECRET KEY ====================
     @Value("${jwt.secret}")
    private String secretKey;
    // must be at least 32 characters


    // ==================== TOKEN EXPIRATION ====================
    private final long EXPIRATION_TIME =
            1000 * 60 * 60; // 1 hour


    // ==================== SIGNING KEY ====================
    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }


    // ==================== GENERATE TOKEN ====================
    public String generateToken(String email, String role) {

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + EXPIRATION_TIME)
                )
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    // ==================== EXTRACT EMAIL ====================
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }


    // ==================== EXTRACT ROLE ====================
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }


    // ==================== VALIDATE TOKEN ====================
    public boolean isTokenValid(String token) {

        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }


    // ==================== EXTRACT ALL CLAIMS ====================
    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}