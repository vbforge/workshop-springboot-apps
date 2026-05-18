package com.vbforge.bookapi.service.impl;

import com.vbforge.bookapi.dto.BookCreateDTO;
import com.vbforge.bookapi.dto.BookDTO;
import com.vbforge.bookapi.dto.BookUpdateDTO;
import com.vbforge.bookapi.entity.Author;
import com.vbforge.bookapi.entity.Book;
import com.vbforge.bookapi.entity.Category;
import com.vbforge.bookapi.entity.Publisher;
import com.vbforge.bookapi.exception.DuplicateResourceException;
import com.vbforge.bookapi.exception.InvalidOperationException;
import com.vbforge.bookapi.exception.ResourceNotFoundException;
import com.vbforge.bookapi.mapper.BookMapper;
import com.vbforge.bookapi.repository.AuthorRepository;
import com.vbforge.bookapi.repository.BookRepository;
import com.vbforge.bookapi.repository.CategoryRepository;
import com.vbforge.bookapi.repository.PublisherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookServiceImpl
 * Uses Mockito to mock dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Book Service Test")
public class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;
    private BookDTO bookDTO;
    private BookCreateDTO bookCreateDTO;
    private Author author;
    private Category category;
    private Publisher publisher;

    @BeforeEach
    void setUp() {
        // Setup test data
        author = Author.builder()
                .id(1L)
                .name("Test Author")
                .build();

        category = Category.builder()
                .id(1L)
                .name("Fiction")
                .build();

        publisher = Publisher.builder()
                .id(1L)
                .name("Test Publisher")
                .build();

        book = Book.builder()
                .id(1L)
                .isbn("978-0-123456-78-9")
                .title("Test Book")
                .description("Test Description")
                .price(new BigDecimal("19.99"))
                .stockQuantity(100)
                .language("English")
                .pageCount(300)
                .publicationDate(LocalDate.of(2024, 1, 1))
                .category(category)
                .publisher(publisher)
                .authors(new HashSet<>(Set.of(author)))
                .build();

        bookDTO = BookDTO.builder()
                .id(1L)
                .isbn("978-0-123456-78-9")
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stockQuantity(100)
                .build();

        bookCreateDTO = BookCreateDTO.builder()
                .isbn("978-0-123456-78-9")
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stockQuantity(100)
                .categoryId(1L)
                .publisherId(1L)
                .authorIds(Set.of(1L))
                .build();

    }

    @Test
    @DisplayName("Should create book successfully")
    public void testCreateBook() {
        //given
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookMapper.toEntity(any(BookCreateDTO.class))).thenReturn(book);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(publisherRepository.findById(anyLong())).thenReturn(Optional.of(publisher));
        when(authorRepository.findById(anyLong())).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.toDTO(any(Book.class))).thenReturn(bookDTO);

        //when
        BookDTO result = bookService.createBook(bookCreateDTO);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getIsbn()).isEqualTo("978-0-123456-78-9");
        assertThat(result.getTitle()).isEqualTo("Test Book");

        verify(bookRepository).existsByIsbn("978-0-123456-78-9");
        verify(bookRepository).save(any(Book.class));
        verify(bookMapper).toDTO(book);
    }

    @Test
    @DisplayName("Should throw exception when ISBN already exists")
    public void testCreateBook_DuplicateISBN() {
        // Given
        when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> bookService.createBook(bookCreateDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ISBN");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    public void testCreateBook_CategoryNotFound() {
        // Given
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookMapper.toEntity(any(BookCreateDTO.class))).thenReturn(book);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.createBook(bookCreateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should get book by ID successfully")
    public void testGetBookById() {
        // Given
        when(bookRepository.findByIdWithDetails(anyLong())).thenReturn(Optional.of(book));
        when(bookMapper.toDTO(any(Book.class))).thenReturn(bookDTO);

        // When
        BookDTO result = bookService.getBookById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(bookRepository).findByIdWithDetails(1L);
        verify(bookMapper).toDTO(book);
    }

    @Test
    @DisplayName("Should throw exception when book not found by ID")
    void testGetBookById_NotFound() {
        // Given
        when(bookRepository.findByIdWithDetails(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.getBookById(9990L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found");

        verify(bookMapper, never()).toDTO(any(Book.class));
    }

    @Test
    @DisplayName("Should get book by ISBN successfully")
    public void testGetBookByIsbn() {
        // Given
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(book));
        when(bookMapper.toDTO(any(Book.class))).thenReturn(bookDTO);

        // When
        BookDTO result = bookService.getBookByIsbn("978-0-123456-78-9");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsbn()).isEqualTo("978-0-123456-78-9");

        verify(bookRepository).findByIsbn("978-0-123456-78-9");
    }

    @Test
    @DisplayName("Should get all books with pagination")
    public void testGetAllBooksWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(book));
        Page<BookDTO> bookDTOPage = new PageImpl<>(List.of(bookDTO));

        when(bookRepository.findAll(any(Pageable.class))).thenReturn(bookPage);
        when(bookMapper.toDTO(any(Book.class))).thenReturn(bookDTO);

        // When
        Page<BookDTO> result = bookService.getAllBooks(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Book");

        verify(bookRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should get all books without pagination")
    public void testGetAllBooks_WithoutPagination() {
        // Given
        when(bookRepository.findAllWithDetails()).thenReturn(List.of(book));
        when(bookMapper.toDTOList(anyList())).thenReturn(List.of(bookDTO));

        // When
        List<BookDTO> result = bookService.getAllBooks();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(bookRepository).findAllWithDetails();
    }

    @Test
    @DisplayName("Should update book successfully")
    public void testUpdateBook() {
        // Given
        BookUpdateDTO updateDTO = BookUpdateDTO.builder()
                .title("Updated Title")
                .price(new BigDecimal("29.99"))
                .build();

        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        doNothing().when(bookMapper).updateEntityFromDTO(any(BookUpdateDTO.class), any(Book.class));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.toDTO(any(Book.class))).thenReturn(bookDTO);

        // When
        BookDTO result = bookService.updateBook(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();

        verify(bookRepository).findById(1L);
        verify(bookMapper).updateEntityFromDTO(updateDTO, book);
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent book")
    public void testUpdateBook_NotFound() {
        // Given
        BookUpdateDTO updateDTO = BookUpdateDTO.builder()
                .title("Updated Title")
                .build();

        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.updateBook(999L, updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should update stock quantity successfully")
    public void testUpdateStockQuantity_Success() {
        // Given
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.toDTO(any(Book.class))).thenReturn(bookDTO);

        // When
        BookDTO result = bookService.updateStockQuantity(1L, 150);

        // Then
        assertThat(result).isNotNull();

        verify(bookRepository).findById(1L);
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("Should throw exception when updating stock with negative quantity")
    public void testUpdateStockQuantity_NegativeQuantity() {
        // When & Then
        assertThatThrownBy(() -> bookService.updateStockQuantity(1L, -10))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Stock quantity cannot be negative");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should delete book successfully")
    public void testDeleteBook() {
        // Given
        when(bookRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(bookRepository).deleteById(anyLong());

        // When
        bookService.deleteBook(1L);

        // Then
        verify(bookRepository).existsById(1L);
        verify(bookRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent book")
    public void testDeleteBook_NotFound() {
        // Given
        when(bookRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> bookService.deleteBook(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found");

        verify(bookRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should search books successfully")
    public void testSearchBooks() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        when(bookRepository.searchBooks(anyString(), any(Pageable.class))).thenReturn(bookPage);
        when(bookMapper.toDTO(any(Book.class))).thenReturn(bookDTO);

        // When
        Page<BookDTO> result = bookService.searchBooks("test", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(bookRepository).searchBooks("test", pageable);
    }

    @Test
    @DisplayName("Should get books by category")
    public void testGetBooksByCategory() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        when(categoryRepository.existsById(anyLong())).thenReturn(true);
        when(bookRepository.findByCategoryId(anyLong(), any(Pageable.class))).thenReturn(bookPage);
        when(bookMapper.toDTO(any(Book.class))).thenReturn(bookDTO);

        // When
        Page<BookDTO> result = bookService.getBooksByCategory(1L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(categoryRepository).existsById(1L);
        verify(bookRepository).findByCategoryId(1L, pageable);
    }

    @Test
    @DisplayName("Should get books by price range")
    public void testGetBooksByPriceRange() {
        // Given
        BigDecimal minPrice = new BigDecimal("10.00");
        BigDecimal maxPrice = new BigDecimal("30.00");

        when(bookRepository.findByPriceBetween(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(List.of(book));
        when(bookMapper.toDTOList(anyList())).thenReturn(List.of(bookDTO));

        // When
        List<BookDTO> result = bookService.getBooksByPriceRange(minPrice, maxPrice);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(bookRepository).findByPriceBetween(minPrice, maxPrice);
    }

    @Test
    @DisplayName("Should throw exception when min price greater than max price")
    public void testGetBooksByPriceRange_InvalidRange() {
        // Given
        BigDecimal minPrice = new BigDecimal("30.00");
        BigDecimal maxPrice = new BigDecimal("10.00");

        // When & Then
        assertThatThrownBy(() -> bookService.getBooksByPriceRange(minPrice, maxPrice))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Min price cannot be greater than max price");

        verify(bookRepository, never()).findByPriceBetween(any(), any());
    }

    @Test
    @DisplayName("Should get books in stock")
    public void testGetBooksInStock() {
        // Given
        when(bookRepository.findBooksInStock()).thenReturn(List.of(book));
        when(bookMapper.toDTOList(anyList())).thenReturn(List.of(bookDTO));

        // When
        List<BookDTO> result = bookService.getBooksInStock();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(bookRepository).findBooksInStock();
    }

    @Test
    @DisplayName("Should get books with low stock")
    public void testGetBooksWithLowStock() {
        // Given
        when(bookRepository.findBooksWithLowStock(anyInt())).thenReturn(List.of(book));
        when(bookMapper.toDTOList(anyList())).thenReturn(List.of(bookDTO));

        // When
        List<BookDTO> result = bookService.getBooksWithLowStock(10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(bookRepository).findBooksWithLowStock(10);
    }

    @Test
    @DisplayName("Should throw exception when low stock threshold is negative")
    public void testGetBooksWithLowStock_NegativeThreshold() {
        // When & Then
        assertThatThrownBy(() -> bookService.getBooksWithLowStock(-5))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Threshold cannot be negative");

        verify(bookRepository, never()).findBooksWithLowStock(anyInt());
    }
}