package com.javaprojects.moonwalk.service.auth;

import com.javaprojects.moonwalk.model.Role;
import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.security.auth.RegistrationRequest;
import com.javaprojects.moonwalk.security.auth.VerificationToken;
import com.javaprojects.moonwalk.security.exceptions.UserAlreadyRegisteredException;
import com.javaprojects.moonwalk.service.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;

    @Transactional
    public void register(RegistrationRequest request) throws MessagingException {
        String email = request.getEmail();
        Optional<User> optUser = userService.findByEmail(email);
        User user;
        boolean tokenNewOrExpired = false;
        if (optUser.isPresent()) {
            user = optUser.get();
            if (!user.getFirstName().equals(request.getFirstName()) ||
                    !user.getLastName().equals(request.getLastName()) ||
                    !bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())
            ) {
                throw new UserAlreadyRegisteredException("User with this email is already registered");
            }
            if (user.isEnabled()) {
                throw new UserAlreadyRegisteredException("User is already Registered and Enabled");
            }
            VerificationToken token = verificationTokenService.findByUser(user);
            if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
                verificationTokenService.deleteByUser(user);
                tokenNewOrExpired = true;
            }
        } else {
            user = new User();
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(email);
            user.setRole(Role.PASSENGER);
            user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
            user.setEnabled(false);
            tokenNewOrExpired = true;
            try {
                userService.save(user);
            } catch (DataIntegrityViolationException ex) {
                throw new UserAlreadyRegisteredException("Email already registered", ex);
            }
        }
        if (tokenNewOrExpired) {
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = VerificationToken
                    .builder()
                    .token(token)
                    .user(user)
                    .expiryDate(LocalDateTime.now().plusDays(1))
                    .build();
            verificationTokenService.save(verificationToken);
            emailService.sendEmail(user.getEmail(), token);
        }
    }

    @Transactional
    public String verifyAndEnableUser(String token) {
        VerificationToken databaseToken = verificationTokenService.findByToken(token);
        if (databaseToken.getConfirmedAt() != null) {
            return "Email is already verified";
        }
        if (databaseToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return "Verification link is expired";
        }
        databaseToken.setConfirmedAt(LocalDateTime.now());
        verificationTokenService.save(databaseToken);
        User user = databaseToken.getUser();
        user.setEnabled(true);
        userService.save(user);
        return "Email verified";
    }
}
