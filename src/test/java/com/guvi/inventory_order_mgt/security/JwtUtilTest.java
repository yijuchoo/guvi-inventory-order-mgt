package com.guvi.inventory_order_mgt.security;

import com.guvi.inventory_order_mgt.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.*;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(60L,
                "test_secret_key_must_be_at_least_32_chars_long");
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String token = jwtUtil.generateToken(1L, Role.USER);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUserId_ShouldReturnCorrectUserId() {
        String token = jwtUtil.generateToken(1L, Role.USER);
        Long userId = jwtUtil.extractUserId(token);
        assertEquals(1L, userId);
    }

    @Test
    void extractRole_ShouldReturnCorrectRole() {
        String token = jwtUtil.generateToken(1L, Role.ADMIN);
        String role = jwtUtil.extractRole(token);
        assertEquals("ADMIN", role);
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenTokenIsValid() {
        String token = jwtUtil.generateToken(1L, Role.USER);
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsInvalid() {
        assertFalse(jwtUtil.isTokenValid("invalid.token.here"));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsExpired() {
        // Create JwtUtil with -1 minute expiration (already expired)
        JwtUtil expiredJwtUtil = new JwtUtil(-1L,
                "test_secret_key_must_be_at_least_32_chars_long");
        String token = expiredJwtUtil.generateToken(1L, Role.USER);
        assertFalse(jwtUtil.isTokenValid(token));
    }

    @Test
    void generateToken_ShouldGenerateDifferentTokens_ForDifferentUsers() {
        String token1 = jwtUtil.generateToken(1L, Role.USER);
        String token2 = jwtUtil.generateToken(2L, Role.USER);
        assertNotEquals(token1, token2);
    }
}