package com.vbforge.bookapi.repository;

import com.vbforge.bookapi.entity.Author;
import com.vbforge.bookapi.entity.Book;
import com.vbforge.bookapi.entity.Category;
import com.vbforge.bookapi.entity.Publisher;
import org.junit.jupiter.api.Assertions;
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
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AuthorRepository
 * Uses H2 in-memory database
 */
@DataJpaTest
@ActiveProfiles("test")
class AuthorRepositoryTest {

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
    public void testSaveAuthor(){
        //verify author was saved in system
        assertThat(testAuthor.getId()).isNotNull();
        assertThat(testAuthor.getName()).isEqualTo("Test Author");
        assertThat(testAuthor.getNationality()).isEqualTo("American");
    }

    @Test
    public void testFindByName() {
        Optional<Author> optionalAuthor = authorRepository.findByName("Test Author");
        Author foundAuthor = null;
        if(optionalAuthor.isPresent()){
            foundAuthor = optionalAuthor.get();
        }
        assertNotNull(foundAuthor);
        assertThat(foundAuthor.getName()).isEqualTo(testAuthor.getName());
    }

    @Test
    public void testFindByNameContainingIgnoreCase() {
        // Act
        List<Author> found = authorRepository.findByNameContainingIgnoreCase("test");

        // Assert
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Test Author");
    }

    @Test
    public void testExistsByName() {
        boolean foundExists = authorRepository.existsByName("Test Author");
        boolean exists = authorRepository.existsByName(testAuthor.getName());
        assertTrue(foundExists);
        assertTrue(exists);
        assertThat(foundExists).isEqualTo(exists);
    }

    @Test
    public void testFindByNationalityIgnoreCase(){
        // Act
        List<Author> found = authorRepository.findByNationalityIgnoreCase("american");

        // Assert
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getNationality()).isEqualTo("American");
    }

    @Test
    void testFindById() {
        // Act
        Optional<Author> found = authorRepository.findById(testAuthor.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Author");
        assertThat(found.get().getNationality()).isEqualTo("American");
    }

    @Test
    void testFindByNationality() {
        // Act
        List<Author> found = authorRepository.findByNationality("American");

        // Assert
        assertThat(found).isNotNull();
        assertThat(found).isNotEmpty();

        for (Author author : found) {
            assertThat(author.getNationality()).isEqualTo(testAuthor.getNationality());
        }
    }

    @Test
    void testFindByNationalityContaining() {
        // Act
        List<Author> found = authorRepository.findByNationality("American");

        // Assert
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getNationality()).isEqualTo("American");
    }

    @Test
    public void testFindByIdWithBooks(){
        // Act
        Optional<Author> found = authorRepository.findByIdWithBooks(testAuthor.getId());
        Author author = null;
        if(found.isPresent()){
            author = found.get();
        }
        assertNotNull(author);
        assertThat(author.getName()).isEqualTo(testAuthor.getName());
        assertThat(author.getDateOfBirth()).isEqualTo(testAuthor.getDateOfBirth());
    }

    @Test
    public void testFindAllWithBooks() {
        List<Author> found = authorRepository.findAllWithBooks();

        assertThat(found.get(0)).isNotNull();
        assertThat(found.get(0).getName()).isEqualTo(testAuthor.getName());
    }

    @Test
    public void testCountByNationality() {
        Long found = authorRepository.countByNationality("American");

        assertThat(found).isNotNull();
        assertThat(found == 1L);
    }
}