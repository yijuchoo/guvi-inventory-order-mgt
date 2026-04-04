package com.guvi.inventory_order_mgt.security;

import com.guvi.inventory_order_mgt.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final long expirationMs;
    private final SecretKey secretKey;

    public JwtUtil(
            @Value("${app.jwt.expirationMinutes}") long expirationMinutes,
            @Value("${app.jwt.secret}") String secret) {
        this.expirationMs = expirationMinutes * 60 * 1000L; // 1440 minutes × 60 × 1000 = 86,400,000 ms = 24 hours
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(bytes);
    }

    // Generate token from userId and role
    public String generateToken(Long userId, Role role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    // Validate token — catches expired, malformed, invalid signature
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    // Extract userId from token subject
    public Long extractUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    // Extract role from token claims
    public String extractRole(String token) {
        return parseClaims(token).get("role").toString();
    }

    // Parse and verify claims
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
