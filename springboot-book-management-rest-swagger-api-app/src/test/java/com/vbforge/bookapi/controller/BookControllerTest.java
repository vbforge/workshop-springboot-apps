package com.vbforge.bookapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbforge.bookapi.dto.BookCreateDTO;
import com.vbforge.bookapi.dto.BookDTO;
import com.vbforge.bookapi.dto.BookUpdateDTO;
import com.vbforge.bookapi.exception.DuplicateResourceException;
import com.vbforge.bookapi.exception.ResourceNotFoundException;
import com.vbforge.bookapi.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BookController
 * Uses MockMvc to test REST endpoints
 */
@WebMvcTest(BookController.class)
@DisplayName("BookController Integration Tests")
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    private BookDTO bookDTO;
    private BookCreateDTO bookCreateDTO;
    private BookUpdateDTO bookUpdateDTO;

    @BeforeEach
    void setUp() {
        bookDTO = BookDTO.builder()
                .id(1L)
                .isbn("978-0-123456-78-9")
                .title("Test Book")
                .description("Test Description")
                .price(new BigDecimal("19.99"))
                .stockQuantity(100)
                .language("English")
                .pageCount(300)
                .build();

        bookCreateDTO = BookCreateDTO.builder()
                .isbn("978-0-123456-78-9")
                .title("Test Book")
                .description("Test Description")
                .price(new BigDecimal("19.99"))
                .stockQuantity(100)
                .language("English")
                .pageCount(300)
                .categoryId(1L)
                .publisherId(1L)
                .authorIds(Set.of(1L))
                .build();

        bookUpdateDTO = BookUpdateDTO.builder()
                .title("Updated Title")
                .price(new BigDecimal("29.99"))
                .build();
    }

    @Test
    @DisplayName("POST /api/books - Should create book successfully")
    public void testCreateBook_Success() throws Exception {
        // Given
        when(bookService.createBook(any(BookCreateDTO.class))).thenReturn(bookDTO);

        // When & Then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Book created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.isbn").value("978-0-123456-78-9"))
                .andExpect(jsonPath("$.data.title").value("Test Book"))
                .andExpect(jsonPath("$.data.price").value(19.99));

        verify(bookService).createBook(any(BookCreateDTO.class));
    }

    @Test
    @DisplayName("POST /api/books - Should return 400 when validation fails")
    public void testCreateBook_ValidationError() throws Exception {
        // Given
        BookCreateDTO invalidDTO = BookCreateDTO.builder()
                .isbn("") // Empty ISBN
                .title("") // Empty title
                .price(new BigDecimal("-10")) // Negative price
                .stockQuantity(-5) // Negative stock
                .build();

        // When & Then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.validationErrors").isArray());

        verify(bookService, never()).createBook(any(BookCreateDTO.class));
    }

    @Test
    @DisplayName("POST /api/books - Should return 409 when ISBN already exists")
    public void testCreateBook_DuplicateISBN() throws Exception {
        // Given
        when(bookService.createBook(any(BookCreateDTO.class)))
                .thenThrow(new DuplicateResourceException("Book", "ISBN", "978-0-123456-78-9"));

        // When & Then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookCreateDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Duplicate Resource"))
                .andExpect(jsonPath("$.message", containsString("ISBN")));
    }

    @Test
    @DisplayName("GET /api/books/{id} - Should get book by ID successfully")
    public void testGetBookById_Success() throws Exception {
        // Given
        when(bookService.getBookById(anyLong())).thenReturn(bookDTO);

        // When & Then
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Book"));

        verify(bookService).getBookById(1L);
    }

    @Test
    @DisplayName("GET /api/books/{id} - Should return 404 when book not found")
    public void testGetBookById_NotFound() throws Exception {
        // Given
        when(bookService.getBookById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Book not found with id: '999'"));

        // When & Then
        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message", containsString("Book not found")));
    }

    @Test
    @DisplayName("GET /api/books/isbn/{isbn} - Should get book by ISBN successfully")
    public void testGetBookByIsbn_Success() throws Exception {
        // Given
        when(bookService.getBookByIsbn(anyString())).thenReturn(bookDTO);

        // When & Then
        mockMvc.perform(get("/api/books/isbn/978-0-123456-78-9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isbn").value("978-0-123456-78-9"));

        verify(bookService).getBookByIsbn("978-0-123456-78-9");
    }

    @Test
    @DisplayName("GET /api/books - Should get all books with pagination")
    public void testGetAllBooks_WithPagination() throws Exception {
        // Given
        Page<BookDTO> bookPage = new PageImpl<>(List.of(bookDTO), PageRequest.of(0, 10), 1);
        when(bookService.getAllBooks(any())).thenReturn(bookPage);

        // When & Then
        mockMvc.perform(get("/api/books")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "title")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("Test Book"))
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(bookService).getAllBooks(any());
    }

    @Test
    @DisplayName("PUT /api/books/{id} - Should update book successfully")
    public void testUpdateBook_Success() throws Exception {
        // Given
        BookDTO updatedBookDTO = BookDTO.builder()
                .id(1L)
                .isbn("978-0-123456-78-9")
                .title("Updated Title")
                .price(new BigDecimal("29.99"))
                .stockQuantity(100)
                .build();

        when(bookService.updateBook(anyLong(), any(BookUpdateDTO.class))).thenReturn(updatedBookDTO);

        // When & Then
        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Book updated successfully"))
                .andExpect(jsonPath("$.data.title").value("Updated Title"))
                .andExpect(jsonPath("$.data.price").value(29.99));

        verify(bookService).updateBook(eq(1L), any(BookUpdateDTO.class));
    }

    @Test
    @DisplayName("PATCH /api/books/{id}/stock - Should update stock successfully")
    public void testUpdateStock_Success() throws Exception {
        // Given
        BookDTO updatedBookDTO = BookDTO.builder()
                .id(1L)
                .isbn("978-0-123456-78-9")
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stockQuantity(150)
                .build();

        when(bookService.updateStockQuantity(anyLong(), anyInt())).thenReturn(updatedBookDTO);

        // When & Then
        mockMvc.perform(patch("/api/books/1/stock")
                        .param("quantity", "150"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock updated successfully"))
                .andExpect(jsonPath("$.data.stockQuantity").value(150));

        verify(bookService).updateStockQuantity(1L, 150);
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - Should delete book successfully")
    public void testDeleteBook_Success() throws Exception {
        // Given
        doNothing().when(bookService).deleteBook(anyLong());

        // When & Then
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Book deleted successfully"));

        verify(bookService).deleteBook(1L);
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - Should return 404 when book not found")
    public void testDeleteBook_NotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Book not found with id: '999'"))
                .when(bookService).deleteBook(anyLong());

        // When & Then
        mockMvc.perform(delete("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }

    @Test
    @DisplayName("GET /api/books/search - Should search books successfully")
    public void testSearchBooks_Success() throws Exception {
        // Given
        Page<BookDTO> bookPage = new PageImpl<>(List.of(bookDTO));
        when(bookService.searchBooks(anyString(), any())).thenReturn(bookPage);

        // When & Then
        mockMvc.perform(get("/api/books/search")
                        .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("Test Book"));

        verify(bookService).searchBooks(eq("test"), any());
    }

    @Test
    @DisplayName("GET /api/books/in-stock - Should get books in stock")
    public void testGetBooksInStock_Success() throws Exception {
        // Given
        when(bookService.getBooksInStock()).thenReturn(List.of(bookDTO));

        // When & Then
        mockMvc.perform(get("/api/books/in-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].stockQuantity").value(100));

        verify(bookService).getBooksInStock();
    }

    @Test
    @DisplayName("GET /api/books/low-stock - Should get books with low stock")
    public void testGetBooksWithLowStock_Success() throws Exception {
        // Given
        when(bookService.getBooksWithLowStock(anyInt())).thenReturn(List.of(bookDTO));

        // When & Then
        mockMvc.perform(get("/api/books/low-stock")
                        .param("threshold", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(bookService).getBooksWithLowStock(10);
    }

}

























