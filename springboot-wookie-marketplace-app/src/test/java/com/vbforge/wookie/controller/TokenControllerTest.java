package com.vbforge.wookie.controller;

import com.vbforge.wookie.dto.request.AuthRequest;
import com.vbforge.wookie.entity.Roles;
import com.vbforge.wookie.entity.User;
import com.vbforge.wookie.repository.UserRepository;
import com.vbforge.wookie.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private AuthRequest authRequest;
    private User testUser;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setUsername("test_user");
        authRequest.setPassword("password123");

        testUser = User.builder()
                .userId(1L)
                .authorPseudonym("test_user")
                .role(Roles.USER)
                .build();

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("test_user")
                .password("encoded_password")
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    void generateToken_Success() throws Exception {
        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        
        // Mock JWT service
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("test.jwt.token");
        
        // Mock user repository
        when(userRepository.findByAuthorPseudonym("test_user")).thenReturn(java.util.Optional.of(testUser));

        // Perform request and verify
        mockMvc.perform(post("/api/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test.jwt.token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("test_user"));
    }

    @Test
    void generateToken_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Mock authentication failure
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Perform request and verify
        mockMvc.perform(post("/api/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());

        // Verify JWT service was never called
        verify(jwtService, never()).generateToken(any(UserDetails.class));
        verify(userRepository, never()).findByAuthorPseudonym(anyString());
    }

    @Test
    void generateToken_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given - empty username and password
        AuthRequest invalidRequest = new AuthRequest();
        invalidRequest.setUsername("");
        invalidRequest.setPassword("");

        // Perform request and verify
        mockMvc.perform(post("/api/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Verify authentication was never called
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

    @Test
    void generateToken_UserNotFound_ReturnsUnauthorized() throws Exception {
        // Mock authentication succeeds but user not found in DB
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Mock user repository - user not found
        when(userRepository.findByAuthorPseudonym("test_user")).thenReturn(java.util.Optional.empty());

        // Perform request and verify - should return 401
        mockMvc.perform(post("/api/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());

        // Verify JWT service was never called
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

}