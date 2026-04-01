package com.vbforge.libraryapi.controller;

import com.vbforge.libraryapi.dto.request.LoginRequest;
import com.vbforge.libraryapi.dto.request.SignupRequest;
import com.vbforge.libraryapi.dto.response.AuthResponse;
import com.vbforge.libraryapi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles HTTP requests, validation, and trimming. Passes raw inputs to services.
 * */

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {

        SignupRequest request = SignupRequest.builder()
                .username(signupRequest.getUsername().trim())
                .email(signupRequest.getEmail().trim())
                .password(signupRequest.getPassword().trim())
                .build();

        AuthResponse response = authService.signup(request);
        log.debug("Signup successful for user: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {

        LoginRequest request = LoginRequest.builder()
                .username(loginRequest.getUsername().trim())
                .password(loginRequest.getPassword().trim())
                .build();

        AuthResponse response = authService.login(request);
        log.debug("Login successful for user: {}", response);
        return ResponseEntity.ok(response);
    }


}
