package com.vbforge.wookie.service.impl;

import com.vbforge.wookie.dto.request.BookCreateRequest;
import com.vbforge.wookie.dto.response.BookResponse;
import com.vbforge.wookie.entity.Book;
import com.vbforge.wookie.entity.Roles;
import com.vbforge.wookie.entity.User;
import com.vbforge.wookie.exception.DuplicateResourceException;
import com.vbforge.wookie.exception.PermissionDeniedException;
import com.vbforge.wookie.exception.ResourceNotFoundException;
import com.vbforge.wookie.repository.BookRepository;
import com.vbforge.wookie.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private User testUser;
    private Book testBook;
    private BookCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .authorPseudonym("test_author")
                .role(Roles.USER)
                .isActive(true)
                .build();

        testBook = Book.builder()
                .bookId(1L)
                .title("Test Book")
                .description("Test Description")
                .author(testUser)
                .coverImage("https://example.com/cover.jpg")
                .price(new BigDecimal("19.99"))
                .isPublished(true)
                .build();

        createRequest = BookCreateRequest.builder()
                .title("New Book")
                .description("New Description")
                .coverImage("https://example.com/new-cover.jpg")
                .price(new BigDecimal("29.99"))
                .build();
    }

    @Test
    void createBook_Success() {
        // Given
        when(userRepository.findByAuthorPseudonym("test_author")).thenReturn(Optional.of(testUser));
        when(bookRepository.findByTitle(createRequest.getTitle())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        BookResponse response = bookService.createBook(createRequest, "test_author");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Test Book");
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void createBook_RestrictedUser_ThrowsException() {
        // Given
        testUser.setRole(Roles.RESTRICTED_USER);
        when(userRepository.findByAuthorPseudonym("darth_vader")).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> bookService.createBook(createRequest, "darth_vader"))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("Restricted users are not allowed to publish books");
        
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void createBook_DuplicateTitle_ThrowsException() {
        // Given
        when(userRepository.findByAuthorPseudonym("test_author")).thenReturn(Optional.of(testUser));
        when(bookRepository.findByTitle(createRequest.getTitle())).thenReturn(Optional.of(testBook));

        // When & Then
        assertThatThrownBy(() -> bookService.createBook(createRequest, "test_author"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
        
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void findBookById_Success() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // When
        Optional<BookResponse> response = bookService.findBookById(1L);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().getBookId()).isEqualTo(1L);
        assertThat(response.get().getTitle()).isEqualTo("Test Book");
    }

    @Test
    void findBookById_NotFound() {
        // Given
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<BookResponse> response = bookService.findBookById(999L);

        // Then
        assertThat(response).isEmpty();
    }

    @Test
    void deleteBook_SoftDelete_Success() {
        // Given
        when(bookRepository.findByTitle("Test Book")).thenReturn(Optional.of(testBook));
        when(userRepository.findByAuthorPseudonym("test_author")).thenReturn(Optional.of(testUser));

        // When
        bookService.deleteBook("Test Book", "test_author");

        // Then
        assertThat(testBook.getIsPublished()).isFalse();
        verify(bookRepository, times(1)).save(testBook);
    }

    @Test
    void deleteBook_NotOwner_ThrowsException() {
        // Given
        User otherUser = User.builder().userId(2L).authorPseudonym("other_user").build();
        testBook.setAuthor(otherUser);
        
        when(bookRepository.findByTitle("Test Book")).thenReturn(Optional.of(testBook));
        when(userRepository.findByAuthorPseudonym("test_author")).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> bookService.deleteBook("Test Book", "test_author"))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("don't have permission");
        
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void togglePublishStatus_Unpublish_Success() {
        // Given
        testBook.setIsPublished(true);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(userRepository.findByAuthorPseudonym("test_author")).thenReturn(Optional.of(testUser));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        BookResponse response = bookService.togglePublishStatus(1L, false, "test_author");

        // Then
        assertThat(response.getIsPublished()).isFalse();
        verify(bookRepository, times(1)).save(testBook);
    }

    @Test
    void togglePublishStatus_Republish_Success() {
        // Given
        testBook.setIsPublished(false);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(userRepository.findByAuthorPseudonym("test_author")).thenReturn(Optional.of(testUser));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        BookResponse response = bookService.togglePublishStatus(1L, true, "test_author");

        // Then
        assertThat(response.getIsPublished()).isTrue();
        verify(bookRepository, times(1)).save(testBook);
    }

    @Test
    void hardDeleteBook_AsAdmin_Success() {
        // Given
        User adminUser = User.builder()
                .userId(99L)
                .authorPseudonym("admin")
                .role(Roles.SUPER_ADMIN)
                .build();
        
        when(userRepository.findByAuthorPseudonym("admin")).thenReturn(Optional.of(adminUser));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        doNothing().when(bookRepository).delete(testBook);

        // When
        bookService.hardDeleteBook(1L, "admin");

        // Then
        verify(bookRepository, times(1)).delete(testBook);
    }

    @Test
    void hardDeleteBook_AsNonAdmin_ThrowsException() {
        // Given
        when(userRepository.findByAuthorPseudonym("test_author")).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> bookService.hardDeleteBook(1L, "test_author"))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("Only SUPER_ADMIN");
        
        verify(bookRepository, never()).delete(any(Book.class));
    }
}