package com.vbforge.booksapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbforge.booksapi.dto.request.BookRequestDTO;
import com.vbforge.booksapi.dto.response.BookResponseDTO;
import com.vbforge.booksapi.entity.Genre;
import com.vbforge.booksapi.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@ActiveProfiles("test")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    private BookRequestDTO bookRequestDTO;
    private BookResponseDTO bookResponseDTO1;
    private BookResponseDTO bookResponseDTO2;
    private BookResponseDTO bookResponseDTO3;

    @BeforeEach
    void setUp() {
        // Initialize Request DTO
        bookRequestDTO = BookRequestDTO.builder()
                .title("Clean Code")
                .description("A handbook of agile software craftsmanship")
                .author("Robert C. Martin")
                .genre(Genre.Education)
                .build();

        // Initialize Response DTOs
        bookResponseDTO1 = BookResponseDTO.builder()
                .id(1L)
                .title("The Pragmatic Programmer")
                .description("A classic software development book")
                .author("David Thomas")
                .genre(Genre.Education)
                .build();

        bookResponseDTO2 = BookResponseDTO.builder()
                .id(2L)
                .title("Harry Potter and the Sorcerer's Stone")
                .description("A magical fantasy adventure")
                .author("J.K. Rowling")
                .genre(Genre.Fantasy)
                .build();

        bookResponseDTO3 = BookResponseDTO.builder()
                .id(3L)
                .title("The Hobbit")
                .description("A fantasy novel about Bilbo Baggins")
                .author("J.R.R. Tolkien")
                .genre(Genre.Fantasy)
                .build();
    }

    // ===== BASIC CRUD ENDPOINTS TESTS =====

    @Test
    void getAllBooks_ShouldReturnListOfBooks_WhenBooksExist() throws Exception {
        // Given
        List<BookResponseDTO> books = List.of(bookResponseDTO1, bookResponseDTO2, bookResponseDTO3);
        when(bookService.getAllBooks()).thenReturn(books);

        // When & Then
        mockMvc.perform(get("/api/book")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("The Pragmatic Programmer"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Harry Potter and the Sorcerer's Stone"))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].title").value("The Hobbit"));

        verify(bookService, times(1)).getAllBooks();
    }

    @Test
    void getAllBooks_ShouldReturnEmptyList_WhenNoBooksExist() throws Exception {
        // Given
        when(bookService.getAllBooks()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/book")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(bookService, times(1)).getAllBooks();
    }

    @Test
    void getBookById_ShouldReturnBook_WhenBookExists() throws Exception {
        // Given
        Long bookId = 1L;
        when(bookService.getBookById(bookId)).thenReturn(bookResponseDTO1);

        // When & Then
        mockMvc.perform(get("/api/book/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("The Pragmatic Programmer"))
                .andExpect(jsonPath("$.author").value("David Thomas"))
                .andExpect(jsonPath("$.genre").value("Education"));

        verify(bookService, times(1)).getBookById(bookId);
    }

    @Test
    void createBook_ShouldReturnCreatedBook_WithStatus201() throws Exception {
        // Given
        BookResponseDTO createdBook = BookResponseDTO.builder()
                .id(4L)
                .title("Clean Code")
                .description("A handbook of agile software craftsmanship")
                .author("Robert C. Martin")
                .genre(Genre.Education)
                .build();

        when(bookService.insertBook(any(BookRequestDTO.class))).thenReturn(createdBook);

        // When & Then
        mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert C. Martin"))
                .andExpect(jsonPath("$.genre").value("Education"));

        verify(bookService, times(1)).insertBook(any(BookRequestDTO.class));
    }

    @Test
    void createBook_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        // Given - Invalid book with empty title
        BookRequestDTO invalidBook = BookRequestDTO.builder()
                .title("")
                .description("Test")
                .author("Test Author")
                .genre(Genre.Education)
                .build();

        // When & Then
        mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).insertBook(any());
    }

    @Test
    void updateBook_ShouldReturnUpdatedBook_WithStatus200() throws Exception {
        // Given
        Long bookId = 1L;
        BookResponseDTO updatedBook = BookResponseDTO.builder()
                .id(1L)
                .title("Clean Code")
                .description("A handbook of agile software craftsmanship")
                .author("Robert C. Martin")
                .genre(Genre.Education)
                .build();

        doNothing().when(bookService).updateBookById(eq(bookId), any(BookRequestDTO.class));
        when(bookService.getBookById(bookId)).thenReturn(updatedBook);

        // When & Then
        mockMvc.perform(put("/api/book/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert C. Martin"));

        verify(bookService, times(1)).updateBookById(eq(bookId), any(BookRequestDTO.class));
        verify(bookService, times(1)).getBookById(bookId);
    }

    @Test
    void deleteBook_ShouldReturnNoContent_WithStatus204() throws Exception {
        // Given
        Long bookId = 1L;
        doNothing().when(bookService).deleteBookById(bookId);

        // When & Then
        mockMvc.perform(delete("/api/book/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteBookById(bookId);
    }

    // ===== ADVANCED QUERY ENDPOINTS TESTS =====

    @Test
    void checkBookExists_ShouldReturnTrue_WhenBookExists() throws Exception {
        // Given
        String title = "The Pragmatic Programmer";
        String author = "David Thomas";
        when(bookService.existsByTitleAndAuthor(title, author)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/book/exists")
                        .param("title", title)
                        .param("author", author)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(bookService, times(1)).existsByTitleAndAuthor(title, author);
    }

    @Test
    void checkBookExists_ShouldReturnFalse_WhenBookDoesNotExist() throws Exception {
        // Given
        String title = "Nonexistent Book";
        String author = "Unknown Author";
        when(bookService.existsByTitleAndAuthor(title, author)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/book/exists")
                        .param("title", title)
                        .param("author", author)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(bookService, times(1)).existsByTitleAndAuthor(title, author);
    }

    @Test
    void getBooksByGenre_ShouldReturnBooks_WhenGenreExists() throws Exception {
        // Given
        Genre genre = Genre.Fantasy;
        List<BookResponseDTO> fantasyBooks = List.of(bookResponseDTO2, bookResponseDTO3);
        when(bookService.getBooksByGenre(genre)).thenReturn(fantasyBooks);

        // When & Then
        mockMvc.perform(get("/api/book/genre/{genre}", genre)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].genre").value("Fantasy"))
                .andExpect(jsonPath("$[1].genre").value("Fantasy"));

        verify(bookService, times(1)).getBooksByGenre(genre);
    }

    @Test
    void getBooksByGenre_ShouldReturnEmptyList_WhenNoBooksInGenre() throws Exception {
        // Given
        Genre genre = Genre.Romance;
        when(bookService.getBooksByGenre(genre)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/book/genre/{genre}", genre)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(bookService, times(1)).getBooksByGenre(genre);
    }

    @Test
    void getBookCountByGenre_ShouldReturnCount_WhenBooksExist() throws Exception {
        // Given
        Genre genre = Genre.Fantasy;
        when(bookService.getBookCountByGenre(genre)).thenReturn(2L);

        // When & Then
        mockMvc.perform(get("/api/book/genre/{genre}/count", genre)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));

        verify(bookService, times(1)).getBookCountByGenre(genre);
    }

    @Test
    void getBookCountByGenre_ShouldReturnZero_WhenNoBooksInGenre() throws Exception {
        // Given
        Genre genre = Genre.Romance;
        when(bookService.getBookCountByGenre(genre)).thenReturn(0L);

        // When & Then
        mockMvc.perform(get("/api/book/genre/{genre}/count", genre)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(bookService, times(1)).getBookCountByGenre(genre);
    }

    @Test
    void searchBooks_ShouldReturnMatchingBooks_WhenKeywordMatches() throws Exception {
        // Given
        String keyword = "Harry";
        List<BookResponseDTO> matchingBooks = List.of(bookResponseDTO2);
        when(bookService.searchBooks(keyword)).thenReturn(matchingBooks);

        // When & Then
        mockMvc.perform(get("/api/book/search")
                        .param("keyword", keyword)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Harry Potter and the Sorcerer's Stone"));

        verify(bookService, times(1)).searchBooks(keyword);
    }

    @Test
    void searchBooks_ShouldReturnEmptyList_WhenNoMatches() throws Exception {
        // Given
        String keyword = "Nonexistent";
        when(bookService.searchBooks(keyword)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/book/search")
                        .param("keyword", keyword)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(bookService, times(1)).searchBooks(keyword);
    }

    @Test
    void advancedSearch_ShouldReturnBooks_WhenTitleProvided() throws Exception {
        // Given
        String title = "Hobbit";
        String author = null;
        List<BookResponseDTO> matchingBooks = List.of(bookResponseDTO3);
        when(bookService.searchBooksByTitleOrAuthor(title, author)).thenReturn(matchingBooks);

        // When & Then
        mockMvc.perform(get("/api/book/advanced-search")
                        .param("title", title)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("The Hobbit"));

        verify(bookService, times(1)).searchBooksByTitleOrAuthor(title, author);
    }

    @Test
    void advancedSearch_ShouldReturnBooks_WhenAuthorProvided() throws Exception {
        // Given
        String title = null;
        String author = "Rowling";
        List<BookResponseDTO> matchingBooks = List.of(bookResponseDTO2);
        when(bookService.searchBooksByTitleOrAuthor(title, author)).thenReturn(matchingBooks);

        // When & Then
        mockMvc.perform(get("/api/book/advanced-search")
                        .param("author", author)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].author").value("J.K. Rowling"));

        verify(bookService, times(1)).searchBooksByTitleOrAuthor(title, author);
    }

    @Test
    void advancedSearch_ShouldReturnAllBooks_WhenNoParamsProvided() throws Exception {
        // Given
        List<BookResponseDTO> allBooks = List.of(bookResponseDTO1, bookResponseDTO2, bookResponseDTO3);
        when(bookService.searchBooksByTitleOrAuthor(null, null)).thenReturn(allBooks);

        // When & Then
        mockMvc.perform(get("/api/book/advanced-search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        verify(bookService, times(1)).searchBooksByTitleOrAuthor(null, null);
    }

    // ===== VALIDATION TESTS =====

    @Test
    void createBook_ShouldReturnBadRequest_WhenTitleIsBlank() throws Exception {
        // Given
        BookRequestDTO invalidBook = BookRequestDTO.builder()
                .title("")
                .description("Valid description")
                .author("Valid Author")
                .genre(Genre.Fiction)
                .build();

        // When & Then
        mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).insertBook(any());
    }

    @Test
    void createBook_ShouldReturnBadRequest_WhenAuthorIsBlank() throws Exception {
        // Given
        BookRequestDTO invalidBook = BookRequestDTO.builder()
                .title("Valid Title")
                .description("Valid description")
                .author("")
                .genre(Genre.Fiction)
                .build();

        // When & Then
        mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).insertBook(any());
    }

    @Test
    void createBook_ShouldReturnBadRequest_WhenGenreIsNull() throws Exception {
        // Given
        BookRequestDTO invalidBook = BookRequestDTO.builder()
                .title("Valid Title")
                .description("Valid description")
                .author("Valid Author")
                .genre(null)
                .build();

        // When & Then
        mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).insertBook(any());
    }

    @Test
    void createBook_ShouldReturnBadRequest_WhenTitleExceedsMaxLength() throws Exception {
        // Given
        String longTitle = "A".repeat(201);
        BookRequestDTO invalidBook = BookRequestDTO.builder()
                .title(longTitle)
                .description("Valid description")
                .author("Valid Author")
                .genre(Genre.Fiction)
                .build();

        // When & Then
        mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).insertBook(any());
    }
}