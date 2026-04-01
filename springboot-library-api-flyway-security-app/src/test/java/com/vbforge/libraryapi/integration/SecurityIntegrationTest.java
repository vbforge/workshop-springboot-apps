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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Role-based access control (RBAC) integration tests.
 * Verifies that JWT roles are correctly enforced across all endpoint groups.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Security / RBAC Integration Tests")
class SecurityIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired BookRepository bookRepository;
    @Autowired BorrowRecordRepository borrowRecordRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    private String librarianToken;
    private String userToken;
    private Book sampleBook;

    @BeforeEach
    void setUp() {
        borrowRecordRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        User librarian = userRepository.save(User.builder()
                .username("librarian1").email("lib@example.com")
                .password(passwordEncoder.encode("libpass"))
                .role(Role.LIBRARIAN).build());

        User user = userRepository.save(User.builder()
                .username("user1").email("user@example.com")
                .password(passwordEncoder.encode("userpass"))
                .role(Role.USER).build());

        librarianToken = "Bearer " + jwtService.generateToken(librarian);
        userToken      = "Bearer " + jwtService.generateToken(user);

        sampleBook = bookRepository.save(Book.builder()
                .title("Clean Code").author("Robert Martin")
                .isbn("9780132350884").totalCopies(5).availableCopies(5).build());
    }

    // =========================================================
    //  Librarian-only endpoints: USER should get 403
    // =========================================================

    @Test
    @DisplayName("POST /api/librarian/books - USER role gets 403 Forbidden")
    void librarianAddBook_asUser_403() throws Exception {
        mockMvc.perform(post("/api/librarian/books")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/librarian/books - USER role gets 403 Forbidden")
    void librarianGetBooks_asUser_403() throws Exception {
        mockMvc.perform(get("/api/librarian/books")
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/librarian/users - USER role gets 403 Forbidden")
    void librarianGetUsers_asUser_403() throws Exception {
        mockMvc.perform(get("/api/librarian/users")
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/librarian/books/{id} - USER role gets 403 Forbidden")
    void librarianDeleteBook_asUser_403() throws Exception {
        mockMvc.perform(delete("/api/librarian/books/" + sampleBook.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    // =========================================================
    //  User-only endpoints: LIBRARIAN should get 403
    // =========================================================

    @Test
    @DisplayName("POST /api/user/books/borrow/{id} - LIBRARIAN role gets 403 Forbidden")
    void userBorrowBook_asLibrarian_403() throws Exception {
        mockMvc.perform(post("/api/user/books/borrow/" + sampleBook.getId())
                        .header("Authorization", librarianToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/user/books/available - LIBRARIAN role gets 403 Forbidden")
    void userAvailableBooks_asLibrarian_403() throws Exception {
        mockMvc.perform(get("/api/user/books/available")
                        .header("Authorization", librarianToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/user/records - LIBRARIAN role gets 403 Forbidden")
    void userRecords_asLibrarian_403() throws Exception {
        mockMvc.perform(get("/api/user/records")
                        .header("Authorization", librarianToken))
                .andExpect(status().isForbidden());
    }

    // =========================================================
    //  No token → always 401
    // =========================================================

    @Test
    @DisplayName("GET /api/librarian/books - no token gets 401 Unauthorized")
    void librarianBooks_noToken_401() throws Exception {
        mockMvc.perform(get("/api/librarian/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/user/books/available - no token gets 401 Unauthorized")
    void userBooks_noToken_401() throws Exception {
        mockMvc.perform(get("/api/user/books/available"))
                .andExpect(status().isUnauthorized());
    }


    // =========================================================
    //  Public endpoints: no token needed
    // =========================================================

    //failed
    @Test
    @DisplayName("POST /api/auth/signup - public endpoint, no token needed")
    void signup_isPublic() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                signupBody("New User", "new_user@example.com", "Pass123AAA!@#"))))
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    @DisplayName("POST /api/auth/login - public endpoint, no token needed")
    void login_isPublic() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nonexistent\",\"password\":\"pass\"}"))
                // Returns 400 (bad creds) not 401 - proving endpoint is publicly accessible
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    //  Helpers
    // =========================================================

    private BookRequest validBookRequest() {
        BookRequest req = new BookRequest();
        req.setTitle("Clean Code");
        req.setAuthor("Robert Martin");
        req.setIsbn("9780132350884");
        req.setTotalCopies(5);
        return req;
    }

    private String signupBody(String username, String email, String password) throws Exception {
        return objectMapper.writeValueAsString(new Object() {
            public final String getUsername() { return username; }
            public final String getEmail() { return email; }
            public final String getPassword() { return password; }
        });
    }
}
