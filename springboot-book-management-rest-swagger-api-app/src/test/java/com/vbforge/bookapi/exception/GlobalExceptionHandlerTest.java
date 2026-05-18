package com.vbforge.bookapi.exception;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbforge.bookapi.controller.BookController;
import com.vbforge.bookapi.dto.BookCreateDTO;
import com.vbforge.bookapi.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for GlobalExceptionHandler
 */
@WebMvcTest(BookController.class)
@DisplayName("GlobalExceptionHandler Tests")
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @Test
    @DisplayName("Should handle ResourceNotFoundException with 404 status")
    void testResourceNotFoundException() throws Exception {
        // Given
        when(bookService.createBook(any())).thenThrow(
                new ResourceNotFoundException("Book", "id", "999")
        );

        BookCreateDTO bookCreateDTO = BookCreateDTO.builder()
                .isbn("978-0-123456-78-9")
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stockQuantity(100)
                .categoryId(1L)
                .publisherId(1L)
                .authorIds(Set.of(1L))
                .build();

        // When & Then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookCreateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Book not found")))
                .andExpect(jsonPath("$.path").value("/api/books"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should handle DuplicateResourceException with 409 status")
    void testDuplicateResourceException() throws Exception {
        // Given
        when(bookService.createBook(any())).thenThrow(
                new DuplicateResourceException("Book", "ISBN", "978-0-123456-78-9")
        );

        BookCreateDTO bookCreateDTO = BookCreateDTO.builder()
                .isbn("978-0-123456-78-9")
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stockQuantity(100)
                .categoryId(1L)
                .publisherId(1L)
                .authorIds(Set.of(1L))
                .build();

        // When & Then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookCreateDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Duplicate Resource"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("already exists")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should handle validation errors with 400 status and field details")
    void testValidationException() throws Exception {
        // Given - Invalid BookCreateDTO with multiple validation errors
        BookCreateDTO invalidDTO = BookCreateDTO.builder()
                .isbn("") // Empty - should fail @NotBlank
                .title("") // Empty - should fail @NotBlank
                .price(new BigDecimal("-10.00")) // Negative - should fail @DecimalMin
                .stockQuantity(-5) // Negative - should fail @Min
                .build();

        // When & Then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should handle InvalidOperationException with 400 status")
    void testInvalidOperationException() throws Exception {
        // Given
        when(bookService.createBook(any())).thenThrow(
                new InvalidOperationException("Invalid operation: cannot create book")
        );

        BookCreateDTO bookCreateDTO = BookCreateDTO.builder()
                .isbn("978-0-123456-78-9")
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stockQuantity(100)
                .categoryId(1L)
                .publisherId(1L)
                .authorIds(Set.of(1L))
                .build();

        // When & Then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid Operation"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Invalid operation")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with 400 status")
    void testIllegalArgumentException() throws Exception {
        // Given
        when(bookService.createBook(any())).thenThrow(
                new IllegalArgumentException("Illegal argument provided")
        );

        BookCreateDTO bookCreateDTO = BookCreateDTO.builder()
                .isbn("978-0-123456-78-9")
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stockQuantity(100)
                .categoryId(1L)
                .publisherId(1L)
                .authorIds(Set.of(1L))
                .build();

        // When & Then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists());
    }

}