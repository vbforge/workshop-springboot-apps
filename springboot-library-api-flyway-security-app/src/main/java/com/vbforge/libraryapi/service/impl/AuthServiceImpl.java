package com.vbforge.libraryapi.service.impl;

import com.vbforge.libraryapi.dto.request.LoginRequest;
import com.vbforge.libraryapi.dto.request.SignupRequest;
import com.vbforge.libraryapi.dto.response.AuthResponse;
import com.vbforge.libraryapi.entity.Role;
import com.vbforge.libraryapi.entity.User;
import com.vbforge.libraryapi.exception.custom.BadRequestException;
import com.vbforge.libraryapi.exception.custom.UserAlreadyExistException;
import com.vbforge.libraryapi.repository.UserRepository;
import com.vbforge.libraryapi.security.JwtService;
import com.vbforge.libraryapi.security.UserDetailsServiceImpl;
import com.vbforge.libraryapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import org.owasp.encoder.Encode;


/**
 * Sanitizes inputs, applies business logic, and interacts with repositories.
 * */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    @Transactional
    public AuthResponse signup(SignupRequest signupRequest) {

        String sanitizedUsername = Encode.forHtml(signupRequest.getUsername().trim());
        String email = signupRequest.getEmail().trim().toLowerCase(); // No sanitization for emails
        String password = signupRequest.getPassword().trim(); // No sanitization for passwords

        if (userRepository.findByUsername(sanitizedUsername).isPresent()) {
            throw new UserAlreadyExistException("Username " + sanitizedUsername + " is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistException("Email " + email + " is already in use");
        }

        User user = User.builder()
                .username(sanitizedUsername)
                .email(email)
                .password(passwordEncoder.encode(password)) // Hash the raw password
                .role(Role.USER)
                .build();

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        log.debug("Login request for name {}", request.getUsername());
        String sanitizedUsername = Encode.forHtml(request.getUsername().trim());
        User user = userRepository.findByUsername(sanitizedUsername)
                .orElseThrow(() -> new BadRequestException("Invalid username"));

        if(!passwordEncoder.matches(request.getPassword().trim(), user.getPassword())){
            log.debug("Password mismatch for user: {}", request.getUsername());
            throw new BadRequestException("Invalid password");
        }

        log.info("User {} has been logged in", user.getUsername());

        return buildAuthResponse(user);
    }

    //helper to build auth response
    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        LocalDateTime expirationDate = jwtService.extractExpirationAsLocalDateTime(token);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .tokenExpiresAt(expirationDate)
                .build();
    }


}

