package com.vbforge.libraryapi.integration;

import com.vbforge.libraryapi.entity.Book;
import com.vbforge.libraryapi.entity.BorrowRecord;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the full borrow/return lifecycle.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Borrow Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BorrowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired BookRepository bookRepository;
    @Autowired UserRepository userRepository;
    @Autowired BorrowRecordRepository borrowRecordRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    private String userToken;
    private User regularUser;
    private Book sampleBook;

    @BeforeEach
    void setUp() {
        borrowRecordRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        regularUser = userRepository.save(User.builder()
                .username("user1").email("user@example.com")
                .password(passwordEncoder.encode("userpass"))
                .role(Role.USER).build());

        sampleBook = bookRepository.save(Book.builder()
                .title("Clean Code").author("Robert Martin")
                .isbn("9780132350884").totalCopies(5).availableCopies(5)
                .build());

        userToken = "Bearer " + jwtService.generateToken(regularUser);
    }

    // =========================================================
    //  POST /api/user/books/borrow/{id}
    // =========================================================

    @Test
    @Order(1)
    @DisplayName("POST /api/user/books/borrow/{id} - user can borrow a book")
    void borrowBook_success() throws Exception {
        mockMvc.perform(post("/api/user/books/borrow/" + sampleBook.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookTitle").value("Clean Code"))
                .andExpect(jsonPath("$.dueDate").isNotEmpty());

        // Available copies should decrease
        Book updated = bookRepository.findById(sampleBook.getId()).orElseThrow();
        assertThat(updated.getAvailableCopies()).isEqualTo(4);
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/user/books/borrow/{id} - should return 400 when book already borrowed by same user")
    void borrowBook_alreadyBorrowed_returns400() throws Exception {
        // First borrow
        mockMvc.perform(post("/api/user/books/borrow/" + sampleBook.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        // Second borrow of same book
        mockMvc.perform(post("/api/user/books/borrow/" + sampleBook.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/user/books/borrow/{id} - should return 400 when borrow limit (3) exceeded")
    void borrowBook_limitExceeded_returns400() throws Exception {
        // Seed 2 more books and borrow them
        Book book2 = bookRepository.save(Book.builder()
                .title("The Pragmatic Programmer").author("Hunt & Thomas")
                .isbn("9780201616224").totalCopies(2).availableCopies(2).build());
        Book book3 = bookRepository.save(Book.builder()
                .title("Refactoring").author("Martin Fowler")
                .isbn("9780134757599").totalCopies(2).availableCopies(2).build());

        mockMvc.perform(post("/api/user/books/borrow/" + sampleBook.getId())
                        .header("Authorization", userToken)).andExpect(status().isOk());
        mockMvc.perform(post("/api/user/books/borrow/" + book2.getId())
                        .header("Authorization", userToken)).andExpect(status().isOk());
        mockMvc.perform(post("/api/user/books/borrow/" + book3.getId())
                        .header("Authorization", userToken)).andExpect(status().isOk());

        // 4th book to push over the limit
        Book book4 = bookRepository.save(Book.builder()
                .title("Design Patterns").author("GoF")
                .isbn("9780201633610").totalCopies(2).availableCopies(2).build());

        mockMvc.perform(post("/api/user/books/borrow/" + book4.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/user/books/borrow/{id} - returns 404 for non-existent book")
    void borrowBook_notFound_returns404() throws Exception {
        mockMvc.perform(post("/api/user/books/borrow/999999")
                        .header("Authorization", userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/user/books/borrow/{id} - returns 401 without auth token")
    void borrowBook_noToken_unauthorized() throws Exception {
        mockMvc.perform(post("/api/user/books/borrow/" + sampleBook.getId()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    //  POST /api/user/books/return/{id}
    // =========================================================

    @Test
    @Order(6)
    @DisplayName("POST /api/user/books/return/{id} - user can return a borrowed book on time (no late fee)")
    void returnBook_onTime_noLateFee() throws Exception {
        // Borrow first
        mockMvc.perform(post("/api/user/books/borrow/" + sampleBook.getId())
                        .header("Authorization", userToken)).andExpect(status().isOk());

        // Return
        mockMvc.perform(post("/api/user/books/return/" + sampleBook.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returnDate").isNotEmpty())
                .andExpect(jsonPath("$.lateFee").value(0.0));

        // Copies should be restored
        Book updated = bookRepository.findById(sampleBook.getId()).orElseThrow();
        assertThat(updated.getAvailableCopies()).isEqualTo(5);
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/user/books/return/{id} - calculates late fee for overdue book")
    void returnBook_late_hasLateFee() throws Exception {
        // Seed a borrow record that is already overdue by 3 days
        BorrowRecord overdueRecord = borrowRecordRepository.save(BorrowRecord.builder()
                .user(regularUser).book(sampleBook)
                .borrowDate(LocalDateTime.now().minusDays(20))
                .dueDate(LocalDateTime.now().minusDays(3)) // 3 days overdue
                .build());

        // Adjust available copies to simulate borrowed state
        sampleBook.setAvailableCopies(4);
        bookRepository.save(sampleBook);

        mockMvc.perform(post("/api/user/books/return/" + sampleBook.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lateFee").value(greaterThan(0.0)));
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/user/books/return/{id} - returns 400 when book was not borrowed")
    void returnBook_notBorrowed_returns400() throws Exception {
        mockMvc.perform(post("/api/user/books/return/" + sampleBook.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    //  GET /api/user/books/available
    // =========================================================

    @Test
    @Order(9)
    @DisplayName("GET /api/user/books/available - returns books with available copies")
    void getAvailableBooks_success() throws Exception {
        // Make sampleBook unavailable
        sampleBook.setAvailableCopies(0);
        bookRepository.save(sampleBook);

        Book available = bookRepository.save(Book.builder()
                .title("Refactoring").author("Martin Fowler")
                .isbn("9780134757599").totalCopies(3).availableCopies(3).build());

        mockMvc.perform(get("/api/user/books/available")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].isbn").value("9780134757599"));
    }

    // =========================================================
    //  GET /api/user/records
    // =========================================================

    @Test
    @Order(10)
    @DisplayName("GET /api/user/records - returns user borrow history")
    void getUserRecords_success() throws Exception {
        // Borrow a book first
        mockMvc.perform(post("/api/user/books/borrow/" + sampleBook.getId())
                        .header("Authorization", userToken)).andExpect(status().isOk());

        mockMvc.perform(get("/api/user/records")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].bookTitle").value("Clean Code"));
    }

    @Test
    @Order(11)
    @DisplayName("GET /api/user/records - returns empty list when no borrows")
    void getUserRecords_empty() throws Exception {
        mockMvc.perform(get("/api/user/records")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
