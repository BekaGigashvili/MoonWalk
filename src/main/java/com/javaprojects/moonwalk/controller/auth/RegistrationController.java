package com.javaprojects.moonwalk.controller.auth;

import com.javaprojects.moonwalk.security.auth.RegistrationRequest;
import com.javaprojects.moonwalk.service.auth.RegistrationService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class RegistrationController {
    private final RegistrationService registrationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationRequest request) throws MessagingException {
        registrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAndEnableToken(@RequestParam("token") String token){
        String response = registrationService.verifyAndEnableUser(token);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
