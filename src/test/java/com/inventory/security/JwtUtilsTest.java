package com.inventory.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilsTest {

    private JwtUtils jwtUtils;
    // 256-bit base64 secret
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    public void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", expirationMs);
    }

    @Test
    public void testGenerateAndValidateToken() {
        String token = jwtUtils.generateTokenFromUsername("testadmin", 1L, "Test Admin", "ADMIN");
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));

        String username = jwtUtils.getUsernameFromJwtToken(token);
        assertEquals("testadmin", username);
    }

    @Test
    public void testInvalidToken() {
        assertFalse(jwtUtils.validateJwtToken("invalid.token.string"));
    }
}
