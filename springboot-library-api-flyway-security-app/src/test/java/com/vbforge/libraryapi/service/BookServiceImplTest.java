package com.vbforge.libraryapi.service;

import com.vbforge.libraryapi.dto.request.BookRequest;
import com.vbforge.libraryapi.dto.response.BookResponse;
import com.vbforge.libraryapi.entity.Book;
import com.vbforge.libraryapi.exception.custom.BadRequestException;
import com.vbforge.libraryapi.exception.custom.BookNotFoundException;
import com.vbforge.libraryapi.exception.custom.IsbnValidationException;
import com.vbforge.libraryapi.repository.BookRepository;
import com.vbforge.libraryapi.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookServiceImpl Unit Tests")
class BookServiceImplTest {

    @Mock private BookRepository bookRepository;

    @InjectMocks private BookServiceImpl bookService;

    private Book sampleBook;
    private BookRequest bookRequest;

    @BeforeEach
    void setUp() {
        sampleBook = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert Martin")
                .isbn("9780132350884")
                .totalCopies(5)
                .availableCopies(5)
                .build();

        bookRequest = new BookRequest();
        bookRequest.setTitle("Clean Code");
        bookRequest.setAuthor("Robert Martin");
        bookRequest.setIsbn("9780132350884");
        bookRequest.setTotalCopies(5);
    }

    // =========================================================
    //  addBook()
    // =========================================================
    @Nested
    @DisplayName("addBook()")
    class AddBookTests {

        @Test
        @DisplayName("should add a book successfully and return BookResponse")
        void addBook_success() {
            when(bookRepository.existsByIsbn("9780132350884")).thenReturn(false);
            when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

            BookResponse response = bookService.addBook(bookRequest);

            assertThat(response).isNotNull();
            assertThat(response.getIsbn()).isEqualTo("9780132350884");
            assertThat(response.getTitle()).isEqualTo("Clean Code");
            assertThat(response.getAvailableCopies()).isEqualTo(5);

            verify(bookRepository).save(argThat(b ->
                    b.getAvailableCopies() == b.getTotalCopies()
            ));
        }

        @Test
        @DisplayName("should throw IsbnValidationException for duplicate ISBN")
        void addBook_duplicateIsbn_throwsException() {
            when(bookRepository.existsByIsbn("9780132350884")).thenReturn(true);

            assertThatThrownBy(() -> bookService.addBook(bookRequest))
                    .isInstanceOf(IsbnValidationException.class);

            verify(bookRepository, never()).save(any());
        }

        @Test
        @DisplayName("should set availableCopies equal to totalCopies on creation")
        void addBook_setsAvailableCopiesToTotal() {
            bookRequest.setTotalCopies(10);
            Book savedBook = Book.builder().id(2L).isbn("9780132350884")
                    .title("Clean Code").author("Robert Martin")
                    .totalCopies(10).availableCopies(10).build();

            when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
            when(bookRepository.save(any())).thenReturn(savedBook);

            BookResponse response = bookService.addBook(bookRequest);

            assertThat(response.getAvailableCopies()).isEqualTo(10);
        }
    }

    // =========================================================
    //  getBookById()
    // =========================================================
    @Nested
    @DisplayName("getBookById()")
    class GetBookByIdTests {

        @Test
        @DisplayName("should return book when found")
        void getBookById_found() {
            when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

            BookResponse response = bookService.getBookById(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("Clean Code");
        }

        @Test
        @DisplayName("should throw BookNotFoundException when book not found")
        void getBookById_notFound() {
            when(bookRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.getBookById(99L))
                    .isInstanceOf(BookNotFoundException.class);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L})
        @DisplayName("should throw BadRequestException for invalid IDs")
        void getBookById_invalidId(Long id) {
            assertThatThrownBy(() -> bookService.getBookById(id))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("should throw BadRequestException for null ID")
        void getBookById_nullId() {
            assertThatThrownBy(() -> bookService.getBookById(null))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    // =========================================================
    //  updateBook()
    // =========================================================
    @Nested
    @DisplayName("updateBook()")
    class UpdateBookTests {

        @Test
        @DisplayName("should update a book successfully")
        void updateBook_success() {
            BookRequest updateRequest = new BookRequest();
            updateRequest.setTitle("Clean Code Updated");
            updateRequest.setAuthor("Robert Martin");
            updateRequest.setIsbn("9780132350884");
            updateRequest.setTotalCopies(8);

            Book updated = Book.builder().id(1L).title("Clean Code Updated")
                    .author("Robert Martin").isbn("9780132350884")
                    .totalCopies(8).availableCopies(8).build();

            when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
            when(bookRepository.save(any())).thenReturn(updated);

            BookResponse response = bookService.updateBook(1L, updateRequest);

            assertThat(response.getTitle()).isEqualTo("Clean Code Updated");
            assertThat(response.getTotalCopies()).isEqualTo(8);
        }

        @Test
        @DisplayName("should throw BookNotFoundException when updating non-existent book")
        void updateBook_notFound() {
            when(bookRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.updateBook(99L, bookRequest))
                    .isInstanceOf(BookNotFoundException.class);
        }

        @Test
        @DisplayName("should throw BadRequestException for null request")
        void updateBook_nullRequest() {
            assertThatThrownBy(() -> bookService.updateBook(1L, null))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    // =========================================================
    //  deleteBookById()
    // =========================================================
    @Nested
    @DisplayName("deleteBookById()")
    class DeleteBookTests {

        @Test
        @DisplayName("should delete book successfully")
        void deleteBook_success() {
            when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
            doNothing().when(bookRepository).deleteById(1L);

            assertThatCode(() -> bookService.deleteBookById(1L)).doesNotThrowAnyException();

            verify(bookRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw BookNotFoundException for non-existent book")
        void deleteBook_notFound() {
            when(bookRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.deleteBookById(99L))
                    .isInstanceOf(BookNotFoundException.class);

            verify(bookRepository, never()).deleteById(any());
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        @DisplayName("should throw BadRequestException for invalid IDs")
        void deleteBook_invalidId(Long id) {
            assertThatThrownBy(() -> bookService.deleteBookById(id))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    // =========================================================
    //  validateIsbn()
    // =========================================================
    @Nested
    @DisplayName("validateIsbn()")
    class ValidateIsbnTests {

        @ParameterizedTest
        @ValueSource(strings = {"978013235088", "97801323508840", "978013235088A", "", "abc"})
        @DisplayName("should throw IsbnValidationException for invalid ISBNs")
        void validateIsbn_invalid(String isbn) {
            assertThatThrownBy(() -> bookService.validateIsbn(isbn))
                    .isInstanceOf(IsbnValidationException.class);
        }

        @Test
        @DisplayName("should throw IsbnValidationException for null ISBN")
        void validateIsbn_null() {
            assertThatThrownBy(() -> bookService.validateIsbn(null))
                    .isInstanceOf(IsbnValidationException.class);
        }

        @Test
        @DisplayName("should not throw for a valid 13-digit ISBN")
        void validateIsbn_valid() {
            assertThatCode(() -> bookService.validateIsbn("9780132350884"))
                    .doesNotThrowAnyException();
        }
    }

    // =========================================================
    //  decreaseAvailableCopies() / increaseAvailableCopies()
    // =========================================================
    @Nested
    @DisplayName("decreaseAvailableCopies() / increaseAvailableCopies()")
    class CopiesTests {

        @Test
        @DisplayName("should decrease available copies by given amount")
        void decreaseAvailableCopies_success() {
            when(bookRepository.findByIsbn("9780132350884")).thenReturn(Optional.of(sampleBook));
            when(bookRepository.save(any())).thenReturn(sampleBook);

            bookService.decreaseAvailableCopies("9780132350884", 1);

            verify(bookRepository).save(argThat(b -> b.getAvailableCopies() == 4));
        }

        @Test
        @DisplayName("should throw BadRequestException when not enough copies available")
        void decreaseAvailableCopies_notEnough() {
            sampleBook.setAvailableCopies(0);
            when(bookRepository.findByIsbn("9780132350884")).thenReturn(Optional.of(sampleBook));

            assertThatThrownBy(() -> bookService.decreaseAvailableCopies("9780132350884", 1))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Not enough copies");
        }

        @Test
        @DisplayName("should increase available copies by given amount")
        void increaseAvailableCopies_success() {
            sampleBook.setAvailableCopies(2);
            when(bookRepository.findByIsbn("9780132350884")).thenReturn(Optional.of(sampleBook));
            when(bookRepository.save(any())).thenReturn(sampleBook);

            bookService.increaseAvailableCopies("9780132350884", 1);

            verify(bookRepository).save(argThat(b -> b.getAvailableCopies() == 3));
        }
    }

    // =========================================================
    //  findAllBooks() / search
    // =========================================================
    @Nested
    @DisplayName("Pagination and Search")
    class PaginationAndSearchTests {

        @Test
        @DisplayName("should return paginated books")
        void findAllBooks_returnsPaginatedResult() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Book> page = new PageImpl<>(List.of(sampleBook));
            when(bookRepository.findAll(pageable)).thenReturn(page);

            Page<BookResponse> result = bookService.findAllBooks(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Clean Code");
        }

        @Test
        @DisplayName("should throw BadRequestException when searching with blank title")
        void searchByTitle_blankTitle_throwsException() {
            assertThatThrownBy(() -> bookService.searchBooksByTitle("  ", PageRequest.of(0, 10)))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("should return available books")
        void getAvailableBooks_success() {
            when(bookRepository.findByAvailableCopiesGreaterThan(0)).thenReturn(List.of(sampleBook));

            List<BookResponse> result = bookService.getAvailableBooks();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAvailableCopies()).isGreaterThan(0);
        }
    }
}
