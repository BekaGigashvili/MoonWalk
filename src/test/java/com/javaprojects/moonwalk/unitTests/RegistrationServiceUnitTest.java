package com.javaprojects.moonwalk.unitTests;

import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.security.auth.RegistrationRequest;
import com.javaprojects.moonwalk.security.auth.VerificationToken;
import com.javaprojects.moonwalk.security.exceptions.UserAlreadyRegisteredException;
import com.javaprojects.moonwalk.service.EmailService;
import com.javaprojects.moonwalk.service.UserService;
import com.javaprojects.moonwalk.service.auth.RegistrationService;
import com.javaprojects.moonwalk.service.auth.VerificationTokenService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationServiceUnitTest {

    private UserService userService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private VerificationTokenService verificationTokenService;
    private EmailService emailService;
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);
        verificationTokenService = mock(VerificationTokenService.class);
        emailService = mock(EmailService.class);

        registrationService = new RegistrationService(
                userService,
                bCryptPasswordEncoder,
                verificationTokenService,
                emailService
        );
    }

    @Test
    void register_savesNewUserAndSendsEmail() throws MessagingException {
        RegistrationRequest request = new RegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("password");

        when(userService.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(bCryptPasswordEncoder.encode("password")).thenReturn("encodedPassword");

        registrationService.register(request);

        verify(userService).save(any(User.class));
        verify(verificationTokenService).save(any(VerificationToken.class));
        verify(emailService).sendVerificationEmail(eq("john@example.com"), anyString());
    }

    @Test
    void register_throwsIfUserAlreadyRegisteredWithDifferentDetails() {
        User existing = new User();
        existing.setFirstName("Jane");
        existing.setLastName("Smith");
        existing.setPassword("encodedPassword");
        existing.setEnabled(false);

        RegistrationRequest request = new RegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("password");

        when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(existing));
        when(bCryptPasswordEncoder.matches("password", "encodedPassword")).thenReturn(false);

        assertThrows(UserAlreadyRegisteredException.class,
                () -> registrationService.register(request));
    }

    @Test
    void register_throwsIfUserAlreadyEnabled() {
        User existing = new User();
        existing.setFirstName("John");
        existing.setLastName("Doe");
        existing.setPassword("encodedPassword");
        existing.setEnabled(true);

        RegistrationRequest request = new RegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("password");

        when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(existing));
        when(bCryptPasswordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        assertThrows(UserAlreadyRegisteredException.class,
                () -> registrationService.register(request));
    }

    @Test
    void register_createsNewVerificationTokenIfExpired() throws MessagingException {
        User existing = new User();
        existing.setFirstName("John");
        existing.setLastName("Doe");
        existing.setPassword("encodedPassword");
        existing.setEmail("john@example.com");
        existing.setEnabled(false);

        VerificationToken oldToken = VerificationToken.builder()
                .expiryDate(LocalDateTime.now().minusDays(1))
                .build();

        RegistrationRequest request = new RegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("password");

        when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(existing));
        when(bCryptPasswordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(verificationTokenService.findByUser(existing)).thenReturn(oldToken);

        registrationService.register(request);

        verify(verificationTokenService).deleteByUser(existing);
        verify(verificationTokenService).save(any(VerificationToken.class));
        verify(emailService).sendVerificationEmail(eq("john@example.com"), anyString());
    }

    @Test
    void verifyAndEnableUser_returnsAlreadyVerifiedIfConfirmedAtNotNull() {
        User user = new User();
        VerificationToken token = VerificationToken.builder()
                .user(user)
                .confirmedAt(LocalDateTime.now())
                .build();

        when(verificationTokenService.findByToken("token123")).thenReturn(token);

        String result = registrationService.verifyAndEnableUser("token123");

        assertEquals("Email is already verified", result);
    }

    @Test
    void verifyAndEnableUser_returnsExpiredIfTokenExpired() {
        User user = new User();
        VerificationToken token = VerificationToken.builder()
                .user(user)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .build();

        when(verificationTokenService.findByToken("token123")).thenReturn(token);

        String result = registrationService.verifyAndEnableUser("token123");

        assertEquals("Verification link is expired", result);
    }

    @Test
    void verifyAndEnableUser_enablesUserIfValid() {
        User user = new User();
        VerificationToken token = VerificationToken.builder()
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        when(verificationTokenService.findByToken("token123")).thenReturn(token);

        String result = registrationService.verifyAndEnableUser("token123");

        assertEquals("Email verified", result);
        assertTrue(user.isEnabled());
        verify(userService).save(user);
        verify(verificationTokenService).save(token);
    }

    @Test
    void register_throwsIfEmailAlreadyRegisteredInDb() {
        RegistrationRequest request = new RegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("password");

        when(userService.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(bCryptPasswordEncoder.encode("password")).thenReturn("encodedPassword");
        doThrow(DataIntegrityViolationException.class).when(userService).save(any(User.class));

        assertThrows(UserAlreadyRegisteredException.class, () -> registrationService.register(request));
    }
}
