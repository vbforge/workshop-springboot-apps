package com.vbforge.libraryapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbforge.libraryapi.dto.request.LoginRequest;
import com.vbforge.libraryapi.dto.request.SignupRequest;
import com.vbforge.libraryapi.entity.Role;
import com.vbforge.libraryapi.entity.User;
import com.vbforge.libraryapi.repository.BorrowRecordRepository;
import com.vbforge.libraryapi.repository.BookRepository;
import com.vbforge.libraryapi.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for /api/auth/** endpoints.
 *
 * Uses H2 in-memory DB via the "test" profile (application-test.yml).
 * Flyway runs all 3 migrations before each test class.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Auth Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired BookRepository bookRepository;
    @Autowired BorrowRecordRepository borrowRecordRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDb() {
        borrowRecordRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    // =========================================================
    //  POST /api/auth/signup
    // =========================================================

    @Test
    @Order(1)
    @DisplayName("POST /api/auth/signup - should register user and return JWT")
    void signup_success() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("Password123#@!");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        assertThat(userRepository.findByUsername("alice")).isPresent();
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/auth/signup - should return 400 for duplicate username")
    void signup_duplicateUsername_returns409() throws Exception {
        // Seed user
        userRepository.save(User.builder()
                .username("alice").email("alice@example.com")
                .password(passwordEncoder.encode("pass"))
                .role(Role.USER).build());

        SignupRequest request = new SignupRequest();
        request.setUsername("alice");
        request.setEmail("alice2@example.com");
        request.setPassword("pass");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // 400
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/auth/signup - should return 400 for duplicate email")
    void signup_duplicateEmail_returns409() throws Exception {
        userRepository.save(User.builder()
                .username("alice").email("alice@example.com")
                .password(passwordEncoder.encode("pass"))
                .role(Role.USER).build());

        SignupRequest request = new SignupRequest();
        request.setUsername("bob");
        request.setEmail("alice@example.com");
        request.setPassword("pass");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    //  POST /api/auth/login
    // =========================================================

    @Test
    @Order(4)
    @DisplayName("POST /api/auth/login - should return JWT for valid credentials")
    void login_success() throws Exception {
        userRepository.save(User.builder()
                .username("alice").email("alice@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER).build());

        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("alice"))
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
        assertThat(token).isNotBlank().startsWith("ey");
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/auth/login - should return 400 for unknown username")
    void login_unknownUsername_returns400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("nobody");
        request.setPassword("pass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/auth/login - should return 400 for wrong password")
    void login_wrongPassword_returns400() throws Exception {
        userRepository.save(User.builder()
                .username("alice").email("alice@example.com")
                .password(passwordEncoder.encode("correctPass"))
                .role(Role.USER).build());

        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("wrongPass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/auth/login - should return 401 for missing token on protected endpoints")
    void protectedEndpoint_noToken_returns401() throws Exception {
        mockMvc.perform(post("/api/librarian/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
