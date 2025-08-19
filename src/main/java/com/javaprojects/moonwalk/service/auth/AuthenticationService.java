package com.javaprojects.moonwalk.service.auth;

import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.security.auth.AuthenticationRequest;
import com.javaprojects.moonwalk.security.auth.AuthenticationResponse;
import com.javaprojects.moonwalk.security.exceptions.WrongEmailOrPasswordException;
import com.javaprojects.moonwalk.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtService jwtService;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String email = request.getEmail();
        User user = userService
                .findByEmail(email)
                .orElseThrow(WrongEmailOrPasswordException::new);
        String password = request.getPassword();
        if (bCryptPasswordEncoder.matches(password, user.getPassword())) {
            String token = jwtService.generateToken(user);
            return AuthenticationResponse
                    .builder()
                    .token(token)
                    .build();
        }else{
            throw new WrongEmailOrPasswordException();
        }
    }

}
