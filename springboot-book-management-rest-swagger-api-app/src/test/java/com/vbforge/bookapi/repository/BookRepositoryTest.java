package com.vbforge.bookapi.repository;

import com.vbforge.bookapi.entity.Author;
import com.vbforge.bookapi.entity.Book;
import com.vbforge.bookapi.entity.Category;
import com.vbforge.bookapi.entity.Publisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for BookRepository
 * Uses H2 in-memory database
 */
@DataJpaTest
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    private Book testBook;
    private Author testAuthor;
    private Category testCategory;
    private Publisher testPublisher;

    @BeforeEach
    void setUp() {
        // Clean database
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        categoryRepository.deleteAll();
        publisherRepository.deleteAll();

        // Create test data
        testAuthor = Author.builder()
                .name("Test Author")
                .nationality("American")
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .build();
        testAuthor = authorRepository.save(testAuthor);

        testCategory = Category.builder()
                .name("Fiction")
                .description("Fiction books")
                .build();
        testCategory = categoryRepository.save(testCategory);

        testPublisher = Publisher.builder()
                .name("Test Publisher")
                .contactEmail("test@publisher.com")
                .build();
        testPublisher = publisherRepository.save(testPublisher);

        testBook = Book.builder()
                .isbn("978-0-123456-78-9")
                .title("Test Book")
                .description("A test book description")
                .publicationDate(LocalDate.of(2023, 1, 1))
                .price(new BigDecimal("19.99"))
                .stockQuantity(100)
                .language("English")
                .pageCount(300)
                .category(testCategory)
                .publisher(testPublisher)
                .build();
        testBook.addAuthor(testAuthor);
        testBook = bookRepository.save(testBook);
    }

    @Test
    void testSaveBook() {
        // Verify book was saved
        assertThat(testBook.getId()).isNotNull();
        assertThat(testBook.getIsbn()).isEqualTo("978-0-123456-78-9");
    }

    @Test
    void testFindById() {
        // Act
        Optional<Book> found = bookRepository.findById(testBook.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Book");
    }

    @Test
    void testFindByIsbn() {
        // Act
        Optional<Book> found = bookRepository.findByIsbn("978-0-123456-78-9");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Book");
    }

    @Test
    void testFindByTitleContaining() {
        // Act
        List<Book> found = bookRepository.findByTitleContainingIgnoreCase("test");

        // Assert
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getTitle()).isEqualTo("Test Book");
    }

    @Test
    void testFindByCategoryId() {
        // Act
        List<Book> found = bookRepository.findByCategoryId(testCategory.getId());

        // Assert
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getCategory().getName()).isEqualTo("Fiction");
    }

    @Test
    void testFindByPublisherId() {
        // Act
        List<Book> found = bookRepository.findByPublisherId(testPublisher.getId());

        // Assert
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getPublisher().getName()).isEqualTo("Test Publisher");
    }

    @Test
    void testFindByAuthorId() {
        // Act
        List<Book> found = bookRepository.findByAuthorId(testAuthor.getId());

        // Assert
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getAuthors()).contains(testAuthor);
    }

    @Test
    void testFindByPriceBetween() {
        // Act
        List<Book> found = bookRepository.findByPriceBetween(
                new BigDecimal("10.00"),
                new BigDecimal("25.00")
        );

        // Assert
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getPrice()).isBetween(
                new BigDecimal("10.00"),
                new BigDecimal("25.00")
        );
    }

    @Test
    void testFindBooksInStock() {
        // Act
        List<Book> found = bookRepository.findBooksInStock();

        // Assert
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStockQuantity()).isGreaterThan(0);
    }

    @Test
    void testFindByIdWithDetails() {
        // Act
        Optional<Book> found = bookRepository.findByIdWithDetails(testBook.getId());

        // Assert
        assertThat(found).isPresent();
        Book book = found.get();
        assertThat(book.getAuthors()).isNotEmpty();
        assertThat(book.getCategory()).isNotNull();
        assertThat(book.getPublisher()).isNotNull();
    }

    @Test
    void testSearchBooks() {
        // Act
        List<Book> found = bookRepository.searchBooks("test");

        // Assert
        assertThat(found).hasSize(1);
    }

    @Test
    void testExistsByIsbn() {
        // Act
        boolean exists = bookRepository.existsByIsbn("978-0-123456-78-9");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void testDeleteBook() {
        // Act
        bookRepository.deleteById(testBook.getId());

        // Assert
        Optional<Book> found = bookRepository.findById(testBook.getId());
        assertThat(found).isEmpty();
    }


}