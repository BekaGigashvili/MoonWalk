package com.javaprojects.moonwalk.repository.auth;

import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.security.auth.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    VerificationToken findByUser(User user);
    void deleteByUser(User user);
    Optional<VerificationToken> findByToken(String token);
}
