package com.javaprojects.moonwalk.unitTests;

import com.javaprojects.moonwalk.service.auth.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceUnitTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();

        Field secretField = JwtService.class.getDeclaredField("SECRET_KEY");
        secretField.setAccessible(true);
        secretField.set(jwtService, "VGhpcyBpcyBhIHRlc3Qgc2VjcmV0IGZvciBKV1Qgc2lnbmluZyBhbmFseXNpcyE=");

        Field expirationField = JwtService.class.getDeclaredField("EXPIRATION_TIME");
        expirationField.setAccessible(true);
        expirationField.set(jwtService, 1000L * 60 * 60); // 1 hour
    }

    @Test
    void generateToken_createsValidTokenWithClaims() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@example.com");

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "PASSENGER");

        String token = jwtService.generateToken(claims, userDetails);

        assertNotNull(token);
        assertTrue(token.contains("."));
        assertEquals("user@example.com", jwtService.extractEmail(token));
        assertEquals("PASSENGER", jwtService.extractClaims(token).get("role"));
    }

    @Test
    void generateToken_withoutClaims_createsToken() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@example.com");

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertEquals("user@example.com", jwtService.extractEmail(token));
    }

    @Test
    void extractClaims_returnsClaimsFromToken() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@example.com");

        String token = jwtService.generateToken(userDetails);

        assertEquals("user@example.com", jwtService.extractClaims(token).getSubject());
    }

    @Test
    void isTokenExpired_returnsFalseForValidToken() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@example.com");

        String token = jwtService.generateToken(userDetails);

        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void validateToken_returnsTrueForValidTokenAndEmail() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@example.com");

        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.validateToken(token, "user@example.com"));
    }

    @Test
    void validateToken_returnsFalseForInvalidEmail() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@example.com");

        String token = jwtService.generateToken(userDetails);

        assertFalse(jwtService.validateToken(token, "other@example.com"));
    }

    @Test
    void validateToken_returnsFalseForExpiredToken() throws Exception {
        Field expirationField = JwtService.class.getDeclaredField("EXPIRATION_TIME");
        expirationField.setAccessible(true);
        expirationField.set(jwtService, 100L);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@example.com");

        String token = jwtService.generateToken(userDetails);

        Thread.sleep(150);

        boolean expired;
        try {
            expired = jwtService.isTokenExpired(token);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            expired = true;
        }

        assertTrue(expired, "Token should be expired");

        boolean valid;
        try {
            valid = jwtService.validateToken(token, "user@example.com");
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            valid = false;
        }

        assertFalse(valid, "Expired token should not be valid");
    }

}
