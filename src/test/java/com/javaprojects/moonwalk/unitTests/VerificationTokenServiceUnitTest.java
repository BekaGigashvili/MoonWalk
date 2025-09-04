package com.javaprojects.moonwalk.unitTests;

import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.security.auth.VerificationToken;
import com.javaprojects.moonwalk.repository.auth.VerificationTokenRepository;
import com.javaprojects.moonwalk.service.auth.VerificationTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VerificationTokenServiceUnitTest {

    private VerificationTokenRepository tokenRepository;
    private VerificationTokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenRepository = mock(VerificationTokenRepository.class);
        tokenService = new VerificationTokenService(tokenRepository);
    }

    @Test
    void findByUser_returnsToken() {
        User user = new User();
        VerificationToken token = new VerificationToken();
        token.setUser(user);

        when(tokenRepository.findByUser(user)).thenReturn(token);

        VerificationToken result = tokenService.findByUser(user);
        assertEquals(token, result);
        verify(tokenRepository).findByUser(user);
    }

    @Test
    void deleteByUser_callsRepository() {
        User user = new User();
        doNothing().when(tokenRepository).deleteByUser(user);

        tokenService.deleteByUser(user);

        verify(tokenRepository).deleteByUser(user);
    }

    @Test
    void save_callsRepository() {
        VerificationToken token = new VerificationToken();

        tokenService.save(token);

        verify(tokenRepository).save(token);
    }

    @Test
    void findByToken_returnsToken() {
        String tokenStr = "abc123";
        VerificationToken token = new VerificationToken();
        token.setToken(tokenStr);

        when(tokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(token));

        VerificationToken result = tokenService.findByToken(tokenStr);
        assertEquals(token, result);
        verify(tokenRepository).findByToken(tokenStr);
    }

    @Test
    void findByToken_throwsIfNotFound() {
        String tokenStr = "nonexistent";
        when(tokenRepository.findByToken(tokenStr)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tokenService.findByToken(tokenStr);
        });

        assertEquals("Token not found", exception.getMessage());
        verify(tokenRepository).findByToken(tokenStr);
    }
}
