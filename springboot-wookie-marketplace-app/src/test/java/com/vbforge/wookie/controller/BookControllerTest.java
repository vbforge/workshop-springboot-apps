package com.vbforge.wookie.controller;

import com.vbforge.wookie.dto.request.BookCreateRequest;
import com.vbforge.wookie.dto.response.BookResponse;
import com.vbforge.wookie.dto.response.UserResponse;
import com.vbforge.wookie.entity.Roles;
import com.vbforge.wookie.entity.User;
import com.vbforge.wookie.service.BookService;
import com.vbforge.wookie.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    private BookCreateRequest createRequest;
    private BookResponse bookResponse;
    private UserResponse authorResponse;

    @BeforeEach
    void setUp() {
        authorResponse = UserResponse.builder()
                .userId(1L)
                .authorPseudonym("test_author")
                .role("USER")
                .isActive(true)
                .build();

        createRequest = BookCreateRequest.builder()
                .title("Test Book")
                .description("Test Description")
                .coverImage("https://example.com/cover.jpg")
                .price(new BigDecimal("19.99"))
                .build();

        bookResponse = BookResponse.builder()
                .bookId(1L)
                .title("Test Book")
                .description("Test Description")
                .author(authorResponse)
                .coverImage("https://example.com/cover.jpg")
                .price(new BigDecimal("19.99"))
                .isPublished(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "test_author", roles = "USER")
    void createBook_Success() throws Exception {
        // Mock static SecurityUtils
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            User currentUser = User.builder()
                    .userId(1L)
                    .authorPseudonym("test_author")
                    .role(Roles.USER)
                    .build();
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(currentUser);
            
            when(bookService.createBook(any(BookCreateRequest.class), anyString())).thenReturn(bookResponse);
            
            mockMvc.perform(post("/api/wookie_books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Test Book"))
                    .andExpect(jsonPath("$.price").value(19.99))
                    .andExpect(jsonPath("$.isPublished").value(true));
            
            verify(bookService, times(1)).createBook(any(BookCreateRequest.class), anyString());
        }
    }

    @Test
    @WithMockUser(username = "DarthVader", roles = "RESTRICTED_USER")
    void createBook_AsRestrictedUser_ReturnsForbidden() throws Exception {
        // Mock static SecurityUtils
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            User restrictedUser = User.builder()
                    .userId(2L)
                    .authorPseudonym("DarthVader")
                    .role(Roles.RESTRICTED_USER)
                    .build();
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(restrictedUser);

            // Mock the service to throw PermissionDeniedException
            when(bookService.createBook(any(BookCreateRequest.class), anyString()))
                    .thenThrow(new com.vbforge.wookie.exception.PermissionDeniedException("Restricted users are not allowed to publish books"));

            mockMvc.perform(post("/api/wookie_books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isForbidden());

            verify(bookService, times(1)).createBook(any(BookCreateRequest.class), anyString());
        }
    }

    @Test
    void getBookById_Success() throws Exception {
        // Given
        when(bookService.findBookById(1L)).thenReturn(java.util.Optional.of(bookResponse));

        // When & Then
        mockMvc.perform(get("/api/wookie_books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.author.authorPseudonym").value("test_author"));
    }

    @Test
    void getBookById_NotFound() throws Exception {
        // Given
        when(bookService.findBookById(999L)).thenReturn(java.util.Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/wookie_books/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchBooks_Success() throws Exception {
        // Given
        org.springframework.data.domain.Page<BookResponse> bookPage =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(bookResponse));
        when(bookService.searchBooks(any(), any(), any(), any(), any())).thenReturn(bookPage);

        // When & Then
        mockMvc.perform(get("/api/wookie_books")
                        .param("title", "Test")
                        .param("minPrice", "10")
                        .param("maxPrice", "50")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Book"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "test_author", roles = "USER")
    void updateBook_Success() throws Exception {
        // Mock static SecurityUtils
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            User currentUser = User.builder()
                    .userId(1L)
                    .authorPseudonym("test_author")
                    .role(Roles.USER)
                    .build();
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(currentUser);

            // Create update request
            com.vbforge.wookie.dto.request.BookUpdateRequest updateRequest =
                    com.vbforge.wookie.dto.request.BookUpdateRequest.builder()
                            .price(new BigDecimal("29.99"))
                            .build();

            BookResponse updatedResponse = BookResponse.builder()
                    .bookId(1L)
                    .title("Test Book")
                    .description("Test Description")
                    .author(authorResponse)
                    .coverImage("https://example.com/cover.jpg")
                    .price(new BigDecimal("29.99"))
                    .isPublished(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(bookService.updateBook(eq(1L), any(), eq("test_author"))).thenReturn(updatedResponse);

            mockMvc.perform(put("/api/wookie_books/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.price").value(29.99));

            verify(bookService, times(1)).updateBook(eq(1L), any(), eq("test_author"));
        }
    }

    @Test
    @WithMockUser(username = "test_author", roles = "USER")
    void deleteBook_Success() throws Exception {
        // Mock static SecurityUtils
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            User currentUser = User.builder()
                    .userId(1L)
                    .authorPseudonym("test_author")
                    .role(Roles.USER)
                    .build();
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(currentUser);

            doNothing().when(bookService).deleteBook(eq("Test Book"), eq("test_author"));

            mockMvc.perform(delete("/api/wookie_books")
                            .param("title", "Test Book"))
                    .andExpect(status().isNoContent());

            verify(bookService, times(1)).deleteBook(eq("Test Book"), eq("test_author"));
        }
    }

    @Test
    @WithMockUser(username = "test_author", roles = "USER")
    void togglePublishStatus_Success() throws Exception {
        // Mock static SecurityUtils
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            User currentUser = User.builder()
                    .userId(1L)
                    .authorPseudonym("test_author")
                    .role(Roles.USER)
                    .build();
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(currentUser);

            BookResponse unpublishedResponse = BookResponse.builder()
                    .bookId(1L)
                    .title("Test Book")
                    .description("Test Description")
                    .author(authorResponse)
                    .coverImage("https://example.com/cover.jpg")
                    .price(new BigDecimal("19.99"))
                    .isPublished(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(bookService.togglePublishStatus(eq(1L), eq(false), eq("test_author")))
                    .thenReturn(unpublishedResponse);

            mockMvc.perform(patch("/api/wookie_books/1/publish")
                            .param("publish", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isPublished").value(false));

            verify(bookService, times(1)).togglePublishStatus(eq(1L), eq(false), eq("test_author"));
        }
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void hardDeleteBook_AsAdmin_Success() throws Exception {
        // Mock static SecurityUtils
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            User adminUser = User.builder()
                    .userId(99L)
                    .authorPseudonym("admin")
                    .role(Roles.SUPER_ADMIN)
                    .build();
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(adminUser);

            doNothing().when(bookService).hardDeleteBook(eq(1L), eq("admin"));

            mockMvc.perform(delete("/api/wookie_books/admin/1"))
                    .andExpect(status().isNoContent());

            verify(bookService, times(1)).hardDeleteBook(eq(1L), eq("admin"));
        }
    }

    @Test
    @WithMockUser(username = "test_author", roles = "USER")
    void getMyBooks_Success() throws Exception {
        // Mock static SecurityUtils
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            User currentUser = User.builder()
                    .userId(1L)
                    .authorPseudonym("test_author")
                    .role(Roles.USER)
                    .build();
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(currentUser);

            when(bookService.findMyBooks("test_author")).thenReturn(java.util.List.of(bookResponse));

            mockMvc.perform(get("/api/wookie_books/me/books"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Test Book"))
                    .andExpect(jsonPath("$.length()").value(1));

            verify(bookService, times(1)).findMyBooks("test_author");
        }
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void getAllBooks_AsAdmin_Success() throws Exception {
        // Mock static SecurityUtils
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            User adminUser = User.builder()
                    .userId(99L)
                    .authorPseudonym("admin")
                    .role(Roles.SUPER_ADMIN)
                    .build();
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(adminUser);

            when(bookService.findAllBooks()).thenReturn(java.util.List.of(bookResponse));

            mockMvc.perform(get("/api/wookie_books/admin/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Test Book"));

            verify(bookService, times(1)).findAllBooks();
        }
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void getUnpublishedBooks_AsAdmin_Success() throws Exception {
        // Mock static SecurityUtils
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            User adminUser = User.builder()
                    .userId(99L)
                    .authorPseudonym("admin")
                    .role(Roles.SUPER_ADMIN)
                    .build();
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(adminUser);

            BookResponse unpublishedBook = BookResponse.builder()
                    .bookId(2L)
                    .title("Unpublished Book")
                    .description("Test Description")
                    .author(authorResponse)
                    .coverImage("https://example.com/cover.jpg")
                    .price(new BigDecimal("9.99"))
                    .isPublished(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(bookService.findAllUnpublishedBooks()).thenReturn(java.util.List.of(unpublishedBook));

            mockMvc.perform(get("/api/wookie_books/admin/unpublished"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Unpublished Book"))
                    .andExpect(jsonPath("$[0].isPublished").value(false));

            verify(bookService, times(1)).findAllUnpublishedBooks();
        }
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void getCleanupCandidates_AsAdmin_Success() throws Exception {
        // Mock static SecurityUtils
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            User adminUser = User.builder()
                    .userId(99L)
                    .authorPseudonym("admin")
                    .role(Roles.SUPER_ADMIN)
                    .build();
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(adminUser);

            BookResponse oldBook = BookResponse.builder()
                    .bookId(3L)
                    .title("Old Unpublished Book")
                    .description("Test Description")
                    .author(authorResponse)
                    .coverImage("https://example.com/cover.jpg")
                    .price(new BigDecimal("5.99"))
                    .isPublished(false)
                    .createdAt(LocalDateTime.now().minusDays(100))
                    .updatedAt(LocalDateTime.now().minusDays(100))
                    .build();

            when(bookService.findAllUnpublishedBooks()).thenReturn(java.util.List.of(oldBook));

            mockMvc.perform(get("/api/wookie_books/admin/cleanup-candidates")
                            .param("daysOld", "90"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Old Unpublished Book"));

            verify(bookService, times(1)).findAllUnpublishedBooks();
        }
    }

    @Test
    @WithMockUser(username = "test_author", roles = "USER")
    void createBook_InvalidData_ReturnsBadRequest() throws Exception {
        // Mock static SecurityUtils
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            User currentUser = User.builder()
                    .userId(1L)
                    .authorPseudonym("test_author")
                    .role(Roles.USER)
                    .build();
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(currentUser);

            // Invalid request with empty title and negative price
            BookCreateRequest invalidRequest = BookCreateRequest.builder()
                    .title("")
                    .description("Short")
                    .coverImage("not-a-url")
                    .price(new BigDecimal("-10"))
                    .build();

            mockMvc.perform(post("/api/wookie_books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(bookService, never()).createBook(any(), anyString());
        }
    }

}