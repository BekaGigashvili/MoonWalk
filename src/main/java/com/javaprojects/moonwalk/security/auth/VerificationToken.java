package com.javaprojects.moonwalk.security.auth;

import com.javaprojects.moonwalk.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String token;
    @OneToOne
    @JoinColumn(name="user_id")
    private User user;
    @Column(nullable = false)
    private LocalDateTime expiryDate;
    private LocalDateTime confirmedAt;
}
