package com.shopease.checkout.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final UUID userId = UUID.fromString("019d0000-0000-7000-8000-000000000001");

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256!!",
                86400000 // 24 hours
        );
    }

    @Test
    void generateTokenReturnsNonNullString() {
        String token = jwtService.generateToken(userId, "kwame@test.com", "GOLD");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void parseTokenExtractsCorrectClaims() {
        String token = jwtService.generateToken(userId, "kwame@test.com", "GOLD");
        var claims = jwtService.parseToken(token);

        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("kwame@test.com", claims.get("email"));
        assertEquals("GOLD", claims.get("tier"));
    }

    @Test
    void getUserIdExtractsCorrectUUID() {
        String token = jwtService.generateToken(userId, "kwame@test.com", "STANDARD");
        UUID extracted = jwtService.getUserId(token);

        assertEquals(userId, extracted);
    }

    @Test
    void isValidReturnsTrueForValidToken() {
        String token = jwtService.generateToken(userId, "kwame@test.com", "STANDARD");
        assertTrue(jwtService.isValid(token));
    }

    @Test
    void isValidReturnsFalseForTamperedToken() {
        String token = jwtService.generateToken(userId, "kwame@test.com", "STANDARD");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertFalse(jwtService.isValid(tampered));
    }

    @Test
    void isValidReturnsFalseForGarbageString() {
        assertFalse(jwtService.isValid("not-a-jwt"));
    }

    @Test
    void isValidReturnsFalseForExpiredToken() {
        // Create a service with 0ms expiration
        var shortLivedService = new JwtService(
                "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256!!",
                0
        );
        String token = shortLivedService.generateToken(userId, "kwame@test.com", "STANDARD");
        assertFalse(shortLivedService.isValid(token));
    }

    @Test
    void tokenSignedWithDifferentKeyIsInvalid() {
        String token = jwtService.generateToken(userId, "kwame@test.com", "STANDARD");

        var otherService = new JwtService(
                "a-completely-different-secret-key-that-is-also-256-bits-long!!!!!",
                86400000
        );
        assertFalse(otherService.isValid(token));
    }
}
