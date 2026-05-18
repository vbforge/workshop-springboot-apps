package com.vbforge.booksapi.service.impl;

import com.vbforge.booksapi.dto.request.BookRequestDTO;
import com.vbforge.booksapi.dto.response.BookResponseDTO;
import com.vbforge.booksapi.entity.Book;
import com.vbforge.booksapi.entity.Genre;
import com.vbforge.booksapi.exception.BookAlreadyExistException;
import com.vbforge.booksapi.exception.BookNotFoundException;
import com.vbforge.booksapi.mapper.BookMapper;
import com.vbforge.booksapi.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BooksServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BooksServiceImpl bookService;

    private Book book1;
    private Book book2;
    private Book book3;
    private BookRequestDTO bookRequestDTO;
    private BookResponseDTO bookResponseDTO1;
    private BookResponseDTO bookResponseDTO2;
    private BookResponseDTO bookResponseDTO3;

    @BeforeEach
    void setUp() {
        // Initialize test entities
        book1 = Book.builder()
                .id(1L)
                .title("The Pragmatic Programmer")
                .description("A classic software development book")
                .author("David Thomas")
                .genre(Genre.Education)
                .build();

        book2 = Book.builder()
                .id(2L)
                .title("Harry Potter and the Sorcerer's Stone")
                .description("A magical fantasy adventure")
                .author("J.K. Rowling")
                .genre(Genre.Fantasy)
                .build();

        book3 = Book.builder()
                .id(3L)
                .title("The Hobbit")
                .description("A fantasy novel about Bilbo Baggins")
                .author("J.R.R. Tolkien")
                .genre(Genre.Fantasy)
                .build();

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

    // ===== TEST 1: getAllBooks =====
    
    @Test
    void getAllBooks_ShouldReturnListOfBookResponseDTOs_WhenBooksExist() {
        // Given
        List<Book> books = List.of(book1, book2, book3);
        List<BookResponseDTO> expectedDTOs = List.of(bookResponseDTO1, bookResponseDTO2, bookResponseDTO3);
        
        when(bookRepository.findAll()).thenReturn(books);
        when(bookMapper.toDTOList(books)).thenReturn(expectedDTOs);

        // When
        List<BookResponseDTO> actualResult = bookService.getAllBooks();

        // Then
        assertThat(actualResult).hasSize(3);
        assertThat(actualResult).isEqualTo(expectedDTOs);
        verify(bookRepository, times(1)).findAll();
        verify(bookMapper, times(1)).toDTOList(books);
    }

    @Test
    void getAllBooks_ShouldReturnEmptyList_WhenNoBooksExist() {
        // Given
        when(bookRepository.findAll()).thenReturn(List.of());
        when(bookMapper.toDTOList(List.of())).thenReturn(List.of());

        // When
        List<BookResponseDTO> actualResult = bookService.getAllBooks();

        // Then
        assertThat(actualResult).isEmpty();
        verify(bookRepository, times(1)).findAll();
    }

    // ===== TEST 2: getBookById =====
    
    @Test
    void getBookById_ShouldReturnBookResponseDTO_WhenBookExists() {
        // Given
        Long bookId = 1L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book1));
        when(bookMapper.toDTO(book1)).thenReturn(bookResponseDTO1);

        // When
        BookResponseDTO actualResult = bookService.getBookById(bookId);

        // Then
        assertThat(actualResult).isNotNull();
        assertThat(actualResult.getId()).isEqualTo(bookId);
        assertThat(actualResult.getTitle()).isEqualTo("The Pragmatic Programmer");
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookMapper, times(1)).toDTO(book1);
    }

    @Test
    void getBookById_ShouldThrowBookNotFoundException_WhenBookDoesNotExist() {
        // Given
        Long bookId = 999L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.getBookById(bookId))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book not found with id: " + bookId);
        
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookMapper, never()).toDTO(any());
    }

    // ===== TEST 3: insertBook =====
    
    @Test
    void insertBook_ShouldCreateAndReturnBookResponseDTO_WhenBookDoesNotExist() {
        // Given
        Book newBook = Book.builder()
                .title("Clean Code")
                .description("A handbook of agile software craftsmanship")
                .author("Robert C. Martin")
                .genre(Genre.Education)
                .build();
        
        Book savedBook = Book.builder()
                .id(4L)
                .title("Clean Code")
                .description("A handbook of agile software craftsmanship")
                .author("Robert C. Martin")
                .genre(Genre.Education)
                .build();
        
        BookResponseDTO expectedResponse = BookResponseDTO.builder()
                .id(4L)
                .title("Clean Code")
                .description("A handbook of agile software craftsmanship")
                .author("Robert C. Martin")
                .genre(Genre.Education)
                .build();
        
        when(bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase(
                bookRequestDTO.getTitle(), bookRequestDTO.getAuthor())).thenReturn(false);
        when(bookMapper.toEntity(bookRequestDTO)).thenReturn(newBook);
        when(bookRepository.save(newBook)).thenReturn(savedBook);
        when(bookMapper.toDTO(savedBook)).thenReturn(expectedResponse);

        // When
        BookResponseDTO actualResult = bookService.insertBook(bookRequestDTO);

        // Then
        assertThat(actualResult).isNotNull();
        assertThat(actualResult.getId()).isEqualTo(4L);
        assertThat(actualResult.getTitle()).isEqualTo("Clean Code");
        verify(bookRepository, times(1)).existsByTitleIgnoreCaseAndAuthorIgnoreCase(anyString(), anyString());
        verify(bookRepository, times(1)).save(newBook);
    }

    @Test
    void insertBook_ShouldThrowBookAlreadyExistException_WhenBookAlreadyExists() {
        // Given
        when(bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase(
                bookRequestDTO.getTitle(), bookRequestDTO.getAuthor())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> bookService.insertBook(bookRequestDTO))
                .isInstanceOf(BookAlreadyExistException.class)
                .hasMessageContaining("already exists");
        
        verify(bookRepository, never()).save(any());
        verify(bookMapper, never()).toEntity(any());
    }

    // ===== TEST 4: updateBookById =====
    
    @Test
    void updateBookById_ShouldUpdateBook_WhenBookExistsAndNoConflict() {
        // Given
        Long bookId = 1L;
        Book existingBook = book1;
        Book updatedBook = Book.builder()
                .id(1L)
                .title("Clean Code")
                .description("A handbook of agile software craftsmanship")
                .author("Robert C. Martin")
                .genre(Genre.Education)
                .build();
        
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase(
                bookRequestDTO.getTitle(), bookRequestDTO.getAuthor())).thenReturn(false);
        doNothing().when(bookMapper).updateEntity(existingBook, bookRequestDTO);
        when(bookRepository.save(existingBook)).thenReturn(updatedBook);

        // When
        bookService.updateBookById(bookId, bookRequestDTO);

        // Then
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).existsByTitleIgnoreCaseAndAuthorIgnoreCase(anyString(), anyString());
        verify(bookMapper, times(1)).updateEntity(existingBook, bookRequestDTO);
        verify(bookRepository, times(1)).save(existingBook);
    }

    @Test
    void updateBookById_ShouldThrowBookNotFoundException_WhenBookDoesNotExist() {
        // Given
        Long bookId = 999L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.updateBookById(bookId, bookRequestDTO))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book not found with id: " + bookId);
        
        verify(bookRepository, never()).save(any());
        verify(bookMapper, never()).updateEntity(any(), any());
    }

    // ===== TEST 5: deleteBookById =====
    
    @Test
    void deleteBookById_ShouldDeleteBook_WhenBookExists() {
        // Given
        Long bookId = 1L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book1));
        doNothing().when(bookRepository).deleteById(bookId);

        // When
        bookService.deleteBookById(bookId);

        // Then
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).deleteById(bookId);
    }

    @Test
    void deleteBookById_ShouldThrowBookNotFoundException_WhenBookDoesNotExist() {
        // Given
        Long bookId = 999L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.deleteBookById(bookId))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book not found with id: " + bookId);
        
        verify(bookRepository, never()).deleteById(any());
    }

    // ===== TEST 6: existsByTitleAndAuthor =====
    
    @Test
    void existsByTitleAndAuthor_ShouldReturnTrue_WhenBookExists() {
        // Given
        String title = "The Pragmatic Programmer";
        String author = "David Thomas";
        when(bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase(title, author)).thenReturn(true);

        // When
        boolean exists = bookService.existsByTitleAndAuthor(title, author);

        // Then
        assertThat(exists).isTrue();
        verify(bookRepository, times(1)).existsByTitleIgnoreCaseAndAuthorIgnoreCase(title, author);
    }

    @Test
    void existsByTitleAndAuthor_ShouldReturnFalse_WhenBookDoesNotExist() {
        // Given
        String title = "Nonexistent Book";
        String author = "Unknown Author";
        when(bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase(title, author)).thenReturn(false);

        // When
        boolean exists = bookService.existsByTitleAndAuthor(title, author);

        // Then
        assertThat(exists).isFalse();
        verify(bookRepository, times(1)).existsByTitleIgnoreCaseAndAuthorIgnoreCase(title, author);
    }

    // ===== TEST 7: getBooksByGenre =====
    
    @Test
    void getBooksByGenre_ShouldReturnListOfBooks_WhenBooksExist() {
        // Given
        Genre genre = Genre.Fantasy;
        List<Book> fantasyBooks = List.of(book2, book3);
        List<BookResponseDTO> expectedDTOs = List.of(bookResponseDTO2, bookResponseDTO3);
        
        when(bookRepository.findByGenre(genre)).thenReturn(fantasyBooks);
        when(bookMapper.toDTOList(fantasyBooks)).thenReturn(expectedDTOs);

        // When
        List<BookResponseDTO> actualResult = bookService.getBooksByGenre(genre);

        // Then
        assertThat(actualResult).hasSize(2);
        assertThat(actualResult).isEqualTo(expectedDTOs);
        verify(bookRepository, times(1)).findByGenre(genre);
    }

    @Test
    void getBooksByGenre_ShouldReturnEmptyList_WhenNoBooksInGenre() {
        // Given
        Genre genre = Genre.Romance;
        when(bookRepository.findByGenre(genre)).thenReturn(List.of());
        when(bookMapper.toDTOList(List.of())).thenReturn(List.of());

        // When
        List<BookResponseDTO> actualResult = bookService.getBooksByGenre(genre);

        // Then
        assertThat(actualResult).isEmpty();
        verify(bookRepository, times(1)).findByGenre(genre);
    }

    // ===== TEST 8: searchBooks =====
    
    @Test
    void searchBooks_ShouldReturnMatchingBooks_WhenKeywordMatches() {
        // Given
        String keyword = "Harry";
        List<Book> matchingBooks = List.of(book2);
        List<BookResponseDTO> expectedDTOs = List.of(bookResponseDTO2);
        
        when(bookRepository.searchByKeyword(keyword)).thenReturn(matchingBooks);
        when(bookMapper.toDTOList(matchingBooks)).thenReturn(expectedDTOs);

        // When
        List<BookResponseDTO> actualResult = bookService.searchBooks(keyword);

        // Then
        assertThat(actualResult).hasSize(1);
        assertThat(actualResult.get(0).getTitle()).contains("Harry Potter");
        verify(bookRepository, times(1)).searchByKeyword(keyword);
    }

    @Test
    void searchBooks_ShouldReturnAllBooks_WhenKeywordIsEmpty() {
        // Given
        String keyword = "";
        List<Book> allBooks = List.of(book1, book2, book3);
        List<BookResponseDTO> expectedDTOs = List.of(bookResponseDTO1, bookResponseDTO2, bookResponseDTO3);
        
        when(bookRepository.findAll()).thenReturn(allBooks);
        when(bookMapper.toDTOList(allBooks)).thenReturn(expectedDTOs);

        // When
        List<BookResponseDTO> actualResult = bookService.searchBooks(keyword);

        // Then
        assertThat(actualResult).hasSize(3);
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void searchBooks_ShouldReturnAllBooks_WhenKeywordIsNull() {
        // Given
        List<Book> allBooks = List.of(book1, book2, book3);
        List<BookResponseDTO> expectedDTOs = List.of(bookResponseDTO1, bookResponseDTO2, bookResponseDTO3);
        
        when(bookRepository.findAll()).thenReturn(allBooks);
        when(bookMapper.toDTOList(allBooks)).thenReturn(expectedDTOs);

        // When
        List<BookResponseDTO> actualResult = bookService.searchBooks(null);

        // Then
        assertThat(actualResult).hasSize(3);
        verify(bookRepository, times(1)).findAll();
    }

    // ===== TEST 9: searchBooksByTitleOrAuthor =====
    
    @Test
    void searchBooksByTitleOrAuthor_ShouldReturnMatchingBooks_WhenTitleMatches() {
        // Given
        String titleKeyword = "Hobbit";
        String authorKeyword = null;
        List<Book> matchingBooks = List.of(book3);
        List<BookResponseDTO> expectedDTOs = List.of(bookResponseDTO3);
        
        when(bookRepository.searchBooksCaseInsensitive(titleKeyword, authorKeyword)).thenReturn(matchingBooks);
        when(bookMapper.toDTOList(matchingBooks)).thenReturn(expectedDTOs);

        // When
        List<BookResponseDTO> actualResult = bookService.searchBooksByTitleOrAuthor(titleKeyword, authorKeyword);

        // Then
        assertThat(actualResult).hasSize(1);
        assertThat(actualResult.get(0).getTitle()).isEqualTo("The Hobbit");
        verify(bookRepository, times(1)).searchBooksCaseInsensitive(titleKeyword, authorKeyword);
    }

    @Test
    void searchBooksByTitleOrAuthor_ShouldReturnMatchingBooks_WhenAuthorMatches() {
        // Given
        String titleKeyword = null;
        String authorKeyword = "Rowling";
        List<Book> matchingBooks = List.of(book2);
        List<BookResponseDTO> expectedDTOs = List.of(bookResponseDTO2);
        
        when(bookRepository.searchBooksCaseInsensitive(titleKeyword, authorKeyword)).thenReturn(matchingBooks);
        when(bookMapper.toDTOList(matchingBooks)).thenReturn(expectedDTOs);

        // When
        List<BookResponseDTO> actualResult = bookService.searchBooksByTitleOrAuthor(titleKeyword, authorKeyword);

        // Then
        assertThat(actualResult).hasSize(1);
        assertThat(actualResult.get(0).getAuthor()).isEqualTo("J.K. Rowling");
        verify(bookRepository, times(1)).searchBooksCaseInsensitive(titleKeyword, authorKeyword);
    }

    @Test
    void searchBooksByTitleOrAuthor_ShouldReturnAllBooks_WhenBothKeywordsAreEmpty() {
        // Given
        String titleKeyword = "";
        String authorKeyword = "";

        // When both are empty strings, they become null after trimming
        List<Book> allBooks = List.of(book1, book2, book3);
        List<BookResponseDTO> expectedDTOs = List.of(bookResponseDTO1, bookResponseDTO2, bookResponseDTO3);

        when(bookRepository.searchBooksCaseInsensitive(null, null)).thenReturn(allBooks);
        when(bookMapper.toDTOList(allBooks)).thenReturn(expectedDTOs);

        // When
        List<BookResponseDTO> actualResult = bookService.searchBooksByTitleOrAuthor(titleKeyword, authorKeyword);

        // Then
        assertThat(actualResult).hasSize(3);
        assertThat(actualResult).isEqualTo(expectedDTOs);
        verify(bookRepository, times(1)).searchBooksCaseInsensitive(null, null);
        verify(bookMapper, times(1)).toDTOList(allBooks);
    }

    // ===== TEST 10: getBookCountByGenre =====
    
    @Test
    void getBookCountByGenre_ShouldReturnCorrectCount_WhenBooksExist() {
        // Given
        Genre genre = Genre.Fantasy;
        List<Book> fantasyBooks = List.of(book2, book3);
        when(bookRepository.findByGenre(genre)).thenReturn(fantasyBooks);

        // When
        long count = bookService.getBookCountByGenre(genre);

        // Then
        assertThat(count).isEqualTo(2);
        verify(bookRepository, times(1)).findByGenre(genre);
    }

    @Test
    void getBookCountByGenre_ShouldReturnZero_WhenNoBooksInGenre() {
        // Given
        Genre genre = Genre.Romance;
        when(bookRepository.findByGenre(genre)).thenReturn(List.of());

        // When
        long count = bookService.getBookCountByGenre(genre);

        // Then
        assertThat(count).isZero();
        verify(bookRepository, times(1)).findByGenre(genre);
    }
}