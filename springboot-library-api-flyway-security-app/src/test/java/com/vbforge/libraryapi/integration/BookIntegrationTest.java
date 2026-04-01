package com.vbforge.libraryapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbforge.libraryapi.dto.request.BookRequest;
import com.vbforge.libraryapi.entity.Book;
import com.vbforge.libraryapi.entity.Role;
import com.vbforge.libraryapi.entity.User;
import com.vbforge.libraryapi.repository.BookRepository;
import com.vbforge.libraryapi.repository.BorrowRecordRepository;
import com.vbforge.libraryapi.repository.UserRepository;
import com.vbforge.libraryapi.security.JwtService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for librarian book management endpoints.
 * Uses H2 + Flyway under "test" profile.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Book (Librarian) Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired BookRepository bookRepository;
    @Autowired UserRepository userRepository;
    @Autowired BorrowRecordRepository borrowRecordRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    private String librarianToken;
    private String userToken;
    private User librarianUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        borrowRecordRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        librarianUser = userRepository.save(User.builder()
                .username("librarian1").email("lib@example.com")
                .password(passwordEncoder.encode("libpass"))
                .role(Role.LIBRARIAN).build());

        regularUser = userRepository.save(User.builder()
                .username("user1").email("user@example.com")
                .password(passwordEncoder.encode("userpass"))
                .role(Role.USER).build());

        librarianToken = "Bearer " + jwtService.generateToken(librarianUser);
        userToken      = "Bearer " + jwtService.generateToken(regularUser);
    }

    // =========================================================
    //  POST /api/librarian/books — Add Book
    // =========================================================

    @Test
    @Order(1)
    @DisplayName("POST /api/librarian/books - librarian can add a book")
    void addBook_asLibrarian_success() throws Exception {
        BookRequest request = validBookRequest("9780132350884");

        mockMvc.perform(post("/api/librarian/books")
                        .header("Authorization", librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn").value("9780132350884"))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.availableCopies").value(5));

        assertThat(bookRepository.existsByIsbn("9780132350884")).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/librarian/books - regular USER is forbidden (403)")
    void addBook_asUser_forbidden() throws Exception {
        mockMvc.perform(post("/api/librarian/books")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookRequest("9780132350884"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/librarian/books - returns 401 without token")
    void addBook_noToken_unauthorized() throws Exception {
        mockMvc.perform(post("/api/librarian/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookRequest("9780132350884"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/librarian/books - returns 400 for invalid ISBN (not 13 digits)")
    void addBook_invalidIsbn_returns400() throws Exception {
        BookRequest request = validBookRequest("12345"); // invalid ISBN

        mockMvc.perform(post("/api/librarian/books")
                        .header("Authorization", librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/librarian/books - returns 400 for duplicate ISBN")
    void addBook_duplicateIsbn_returns400() throws Exception {
        bookRepository.save(bookEntity("9780132350884"));

        mockMvc.perform(post("/api/librarian/books")
                        .header("Authorization", librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookRequest("9780132350884"))))
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    //  GET /api/librarian/books — Paginated
    // =========================================================

    @Test
    @Order(6)
    @DisplayName("GET /api/librarian/books - librarian can list all books paginated")
    void getBooks_asLibrarian_success() throws Exception {
        bookRepository.save(bookEntity("9780132350884"));
        bookRepository.save(bookEntity("9780134685991"));

        mockMvc.perform(get("/api/librarian/books?page=0&size=10")
                        .header("Authorization", librarianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    // =========================================================
    //  PUT /api/librarian/books/{id} — Update Book
    // =========================================================

    @Test
    @Order(7)
    @DisplayName("PUT /api/librarian/books/{id} - librarian can update a book")
    void updateBook_asLibrarian_success() throws Exception {
        Book saved = bookRepository.save(bookEntity("9780132350884"));

        BookRequest updateRequest = validBookRequest("9780132350884");
        updateRequest.setTitle("Clean Code - 2nd Edition");
        updateRequest.setTotalCopies(10);

        mockMvc.perform(put("/api/librarian/books/" + saved.getId())
                        .header("Authorization", librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code - 2nd Edition"))
                .andExpect(jsonPath("$.totalCopies").value(10));
    }

    @Test
    @Order(8)
    @DisplayName("PUT /api/librarian/books/{id} - returns 404 for non-existent book")
    void updateBook_notFound_returns404() throws Exception {
        mockMvc.perform(put("/api/librarian/books/999999")
                        .header("Authorization", librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookRequest("9780132350884"))))
                .andExpect(status().isNotFound());
    }

    // =========================================================
    //  DELETE /api/librarian/books/{id} — Delete Book
    // =========================================================

    @Test
    @Order(9)
    @DisplayName("DELETE /api/librarian/books/{id} - librarian can delete a book")
    void deleteBook_asLibrarian_success() throws Exception {
        Book saved = bookRepository.save(bookEntity("9780132350884"));

        mockMvc.perform(delete("/api/librarian/books/" + saved.getId())
                        .header("Authorization", librarianToken))
                .andExpect(status().isNoContent());

        assertThat(bookRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @Order(10)
    @DisplayName("DELETE /api/librarian/books/{id} - returns 404 for non-existent book")
    void deleteBook_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/librarian/books/999999")
                        .header("Authorization", librarianToken))
                .andExpect(status().isNotFound());
    }

    // =========================================================
    //  GET /api/librarian/books/search/title
    // =========================================================

    @Test
    @Order(11)
    @DisplayName("GET /api/librarian/books/search/title - returns matching books")
    void searchByTitle_success() throws Exception {
        bookRepository.save(bookEntity("9780132350884")); // "Clean Code"
        bookRepository.save(bookEntity("9780134685991")); // "Clean Architecture"

        mockMvc.perform(get("/api/librarian/books/search/title?title=Clean&page=0&size=10")
                        .header("Authorization", librarianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    // =========================================================
    //  Helpers
    // =========================================================

    private BookRequest validBookRequest(String isbn) {
        BookRequest req = new BookRequest();
        req.setTitle("Clean Code");
        req.setAuthor("Robert Martin");
        req.setIsbn(isbn);
        req.setTotalCopies(5);
        return req;
    }

    private Book bookEntity(String isbn) {
        String title = isbn.equals("9780132350884") ? "Clean Code" : "Clean Architecture";
        return Book.builder()
                .title(title).author("Robert Martin")
                .isbn(isbn).totalCopies(5).availableCopies(5)
                .build();
    }
}
