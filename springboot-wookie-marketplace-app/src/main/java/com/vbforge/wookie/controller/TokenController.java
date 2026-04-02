package com.vbforge.wookie.controller;

import com.vbforge.wookie.dto.request.AuthRequest;
import com.vbforge.wookie.dto.response.AuthResponse;
import com.vbforge.wookie.repository.UserRepository;
import com.vbforge.wookie.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${app.token.expiration}")
    private Long EXPIRATION_TIME;

    @PostMapping
    public ResponseEntity<AuthResponse> generateToken(@Valid @RequestBody AuthRequest request) {
        log.debug("Authentication request for user: {}", request.getUsername());
        
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            
            // Verify user exists in database and is active
            var userOpt = userRepository.findByAuthorPseudonym(request.getUsername());
            if (userOpt.isEmpty() || !userOpt.get().getIsActive()) {
                log.warn("User authenticated but not found or inactive in database: {}", request.getUsername());
                throw new BadCredentialsException("Invalid credentials");
            }
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            String role = userDetails.getAuthorities().iterator().next().getAuthority();
            
            log.info("User authenticated successfully: {}", request.getUsername());
            
            return ResponseEntity.ok(new AuthResponse(
                    token,
                    userDetails.getUsername(),
                    role,
                    EXPIRATION_TIME / 1000
            ));
            
        } catch (BadCredentialsException e) {
            log.error("Authentication failed for user {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Authentication failed for user {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}