package com.vbforge.wookie.controller;

import com.vbforge.wookie.dto.request.UserCreateRequest;
import com.vbforge.wookie.dto.response.UserResponse;
import com.vbforge.wookie.entity.Roles;
import com.vbforge.wookie.entity.User;
import com.vbforge.wookie.repository.UserRepository;
import com.vbforge.wookie.service.UserService;
import com.vbforge.wookie.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private UserCreateRequest registerRequest;
    private User testUser;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        registerRequest = UserCreateRequest.builder()
                .authorPseudonym("new_author")
                .authorPassword("password123")
                .build();

        testUser = User.builder()
                .userId(1L)
                .authorPseudonym("new_author")
                .role(Roles.USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        userResponse = UserResponse.builder()
                .userId(1L)
                .authorPseudonym("new_author")
                .role("USER")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void registerUser_Success() throws Exception {
        when(userService.existsByPseudonym(anyString())).thenReturn(false);
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorPseudonym").value("new_author"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    void registerUser_DuplicatePseudonym_ReturnsConflict() throws Exception {
        when(userService.existsByPseudonym(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void registerUser_InvalidData_ReturnsBadRequest() throws Exception {
        UserCreateRequest invalidRequest = UserCreateRequest.builder()
                .authorPseudonym("ab")
                .authorPassword("123")
                .build();

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).existsByPseudonym(anyString());
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void registerUser_RestrictedUser_DarthVader() throws Exception {
        UserCreateRequest darthRequest = UserCreateRequest.builder()
                .authorPseudonym("DarthVader")
                .authorPassword("force123")
                .build();

        User darthUser = User.builder()
                .userId(2L)
                .authorPseudonym("DarthVader")
                .role(Roles.RESTRICTED_USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.existsByPseudonym("DarthVader")).thenReturn(false);
        when(userService.createUser(any(User.class))).thenReturn(darthUser);

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(darthRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorPseudonym").value("DarthVader"))
                .andExpect(jsonPath("$.role").value("RESTRICTED_USER"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    @WithMockUser(username = "new_author", roles = "USER")
    void getUserById_Success() throws Exception {
        // Mock static SecurityUtils method
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(() -> SecurityUtils.isOwnerOrAdmin(1L)).thenReturn(true);
            when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

            mockMvc.perform(get("/api/user/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.authorPseudonym").value("new_author"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }
    }

    @Test
    @WithMockUser(username = "new_author", roles = "USER")
    void getUserById_NotFound() throws Exception {
        // Mock static SecurityUtils method
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(() -> SecurityUtils.isOwnerOrAdmin(999L)).thenReturn(true);
            when(userService.getUserById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/user/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @WithMockUser(username = "new_author", roles = "USER")
    void deleteUserById_Success() throws Exception {
        // Mock static SecurityUtils method
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(() -> SecurityUtils.isOwnerOrAdmin(1L)).thenReturn(true);
            when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
            when(userService.deleteUserById(1L)).thenReturn(true);

            mockMvc.perform(delete("/api/user/1"))
                    .andExpect(status().isNoContent());

            verify(userService, times(1)).deleteUserById(1L);
        }
    }

    @Test
    @WithMockUser(username = "new_author", roles = "USER")
    void deleteUserById_Success2() throws Exception {
        // Mock static SecurityUtils method
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(() -> SecurityUtils.isOwnerOrAdmin(1L)).thenReturn(true);
            when(userService.deleteUserById(1L)).thenReturn(true);

            mockMvc.perform(delete("/api/user/1"))
                    .andExpect(status().isNoContent());

            verify(userService, times(1)).deleteUserById(1L);
        }
    }

    @Test
    @WithMockUser(username = "new_author", roles = "USER")
    void deleteUserById_NotFound() throws Exception {
        // Mock static SecurityUtils method
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(() -> SecurityUtils.isOwnerOrAdmin(999L)).thenReturn(true);
            // Mock deleteUserById to return false (user not found)
            when(userService.deleteUserById(999L)).thenReturn(false);

            mockMvc.perform(delete("/api/user/999"))
                    .andExpect(status().isNotFound());

            verify(userService, times(1)).deleteUserById(999L);
        }
    }

    @Test
    @WithMockUser(username = "new_author", roles = "USER")
    void updateUser_Success() throws Exception {
        // Mock static SecurityUtils method
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(() -> SecurityUtils.isOwnerOrAdmin(1L)).thenReturn(true);

            UserCreateRequest updateRequest = UserCreateRequest.builder()
                    .authorPseudonym("updated_author")
                    .authorPassword("newPassword123")
                    .build();

            User updatedUser = User.builder()
                    .userId(1L)
                    .authorPseudonym("updated_author")
                    .role(Roles.USER)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
            when(userService.existsByPseudonym("updated_author")).thenReturn(false);
            when(userService.createUser(any(User.class))).thenReturn(updatedUser);

            mockMvc.perform(put("/api/user/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.authorPseudonym").value("updated_author"));

            verify(userService, times(1)).createUser(any(User.class));
        }
    }

    @Test
    @WithMockUser(username = "new_author", roles = "USER")
    void updateUser_NotFound() throws Exception {
        // Mock static SecurityUtils method
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(() -> SecurityUtils.isOwnerOrAdmin(999L)).thenReturn(true);

            UserCreateRequest updateRequest = UserCreateRequest.builder()
                    .authorPseudonym("updated_author")
                    .authorPassword("newPassword123")
                    .build();

            when(userService.getUserById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(put("/api/user/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());

            verify(userService, never()).createUser(any(User.class));
        }
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void findAllUsers_AsAdmin_Success() throws Exception {
        // Mock static SecurityUtils methods
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            // Create admin user
            User adminUser = User.builder()
                    .userId(99L)
                    .authorPseudonym("admin")
                    .role(Roles.SUPER_ADMIN)
                    .isActive(true)
                    .build();

            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(adminUser);

            User anotherUser = User.builder()
                    .userId(2L)
                    .authorPseudonym("another_author")
                    .role(Roles.USER)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userService.findAllUsers()).thenReturn(java.util.List.of(testUser, anotherUser));

            // When & Then
            mockMvc.perform(get("/api/user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].authorPseudonym").value("new_author"))
                    .andExpect(jsonPath("$[1].authorPseudonym").value("another_author"))
                    .andExpect(jsonPath("$.length()").value(2));

            verify(userService, times(1)).findAllUsers();
        }
    }

    @Test
    @WithMockUser(roles = "USER")
    void findAllUsers_AsUser_ReturnsForbidden() throws Exception {
        // Mock static SecurityUtils method
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            // Create regular user
            User regularUser = User.builder()
                    .userId(1L)
                    .authorPseudonym("new_author")
                    .role(Roles.USER)
                    .isActive(true)
                    .build();

            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(regularUser);

            // When & Then
            mockMvc.perform(get("/api/user"))
                    .andExpect(status().isForbidden());

            verify(userService, never()).findAllUsers();
        }
    }

    @Test
    @WithMockUser(username = "new_author", roles = "USER")
    void findUserByAuthorPseudonym_Success() throws Exception {
        // Mock static SecurityUtils method
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(() -> SecurityUtils.isOwnerOrAdmin(1L)).thenReturn(true);
            when(userService.findUserByAuthorPseudonym("new_author")).thenReturn(Optional.of(testUser));

            mockMvc.perform(get("/api/user/pseudonym/new_author"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.authorPseudonym").value("new_author"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }
    }

    @Test
    @WithMockUser(username = "new_author", roles = "USER")
    void findUserByAuthorPseudonym_NotFound() throws Exception {
        // Mock static SecurityUtils method
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            when(userService.findUserByAuthorPseudonym("nonexistent")).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/user/pseudonym/nonexistent"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @WithMockUser(username = "new_author", roles = "USER")
    void updateAuthorPseudonym_Success() throws Exception {
        // Mock static SecurityUtils method
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            // First, find the user by current pseudonym
            when(userService.findUserByAuthorPseudonym("new_author")).thenReturn(Optional.of(testUser));
            // Mock ownership check
            securityUtilsMock.when(() -> SecurityUtils.isOwnerOrAdmin(1L)).thenReturn(true);
            // Mock the update operation
            User updatedUser = User.builder()
                    .userId(1L)
                    .authorPseudonym("brand_new_pseudonym")
                    .role(Roles.USER)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            when(userService.updateAuthorPseudonym("new_author", "brand_new_pseudonym"))
                    .thenReturn(Optional.of(updatedUser));

            mockMvc.perform(put("/api/user/pseudonym/new_author")
                            .param("newPseudonym", "brand_new_pseudonym"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.authorPseudonym").value("brand_new_pseudonym"));

            verify(userService, times(1)).updateAuthorPseudonym("new_author", "brand_new_pseudonym");
        }
    }

    @Test
    @WithMockUser(username = "new_author", roles = "USER")
    void updateAuthorPseudonym_NotFound() throws Exception {
        // Mock static SecurityUtils method
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            when(userService.findUserByAuthorPseudonym("nonexistent")).thenReturn(Optional.empty());

            mockMvc.perform(put("/api/user/pseudonym/nonexistent")
                            .param("newPseudonym", "new_name"))
                    .andExpect(status().isNotFound());

            verify(userService, never()).updateAuthorPseudonym(anyString(), anyString());
        }
    }


}