package com.javaprojects.moonwalk.controller.auth;

import com.javaprojects.moonwalk.security.auth.AuthenticationRequest;
import com.javaprojects.moonwalk.security.auth.AuthenticationResponse;
import com.javaprojects.moonwalk.service.auth.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest authenticationRequest
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authenticationService.authenticate(authenticationRequest));
    }
}
