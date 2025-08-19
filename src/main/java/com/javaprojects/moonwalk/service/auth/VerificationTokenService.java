package com.javaprojects.moonwalk.service.auth;

import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.security.auth.VerificationToken;
import com.javaprojects.moonwalk.repository.auth.VerificationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;

    public VerificationToken findByUser(User user) {
        return verificationTokenRepository.findByUser(user);
    }

    public void deleteByUser(User user) {
        verificationTokenRepository.deleteByUser(user);
    }
    public void save(VerificationToken verificationToken) {
        verificationTokenRepository.save(verificationToken);
    }
    public VerificationToken findByToken(String token) {
        return verificationTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));
    }
}
