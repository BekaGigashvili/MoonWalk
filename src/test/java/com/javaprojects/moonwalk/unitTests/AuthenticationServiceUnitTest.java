package com.javaprojects.moonwalk.unitTests;

import com.javaprojects.moonwalk.model.Role;
import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.security.auth.AuthenticationRequest;
import com.javaprojects.moonwalk.security.auth.AuthenticationResponse;
import com.javaprojects.moonwalk.security.exceptions.WrongEmailOrPasswordException;
import com.javaprojects.moonwalk.service.UserService;
import com.javaprojects.moonwalk.service.auth.AuthenticationService;
import com.javaprojects.moonwalk.service.auth.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceUnitTest {

    private UserService userService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private JwtService jwtService;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);
        jwtService = mock(JwtService.class);

        authenticationService = new AuthenticationService(
                userService,
                bCryptPasswordEncoder,
                jwtService
        );
    }

    @Test
    void authenticate_returnsTokenAndRole_whenCredentialsAreCorrect() {
        User user = new User();
        user.setPassword("encodedPassword");
        user.setRole(Role.PASSENGER);

        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("test@example.com");
        request.setPassword("rawPassword");

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertEquals("jwtToken", response.getToken());
        assertEquals(Role.PASSENGER, response.getRole());
    }

    @Test
    void authenticate_throwsIfUserNotFound() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("missing@example.com");
        request.setPassword("password");

        when(userService.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(WrongEmailOrPasswordException.class,
                () -> authenticationService.authenticate(request));
    }

    @Test
    void authenticate_throwsIfPasswordDoesNotMatch() {
        User user = new User();
        user.setPassword("encodedPassword");
        user.setRole(Role.PASSENGER);

        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(WrongEmailOrPasswordException.class,
                () -> authenticationService.authenticate(request));
    }
}
