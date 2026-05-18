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
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CategoryRepository
 * Uses H2 in-memory database
 */
@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

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
    public void testFindByName() {
        //act
        Optional<Category> found = categoryRepository.findByName("Fiction");
        Category expected = null;
        if(found.isPresent()){
            expected = found.get();
        }

        //assert
        assertNotNull(expected);
        assertThat(expected.getName()).isEqualTo(testCategory.getName());
    }

    @Test
    public void testFindByNameContainingIgnoreCase() {
        //act
        List<Category> found = categoryRepository.findByNameContainingIgnoreCase("fiction");

        //assert
        assertThat(found).isNotNull();
        assertThat(found.get(0).getName()).isEqualTo(testCategory.getName());
    }

    @Test
    public void testExistsByName() {
        //act
        boolean exists = categoryRepository.existsByName("Fiction");
        boolean not_exists = categoryRepository.existsByName("Action");

        //assert
        assertTrue(exists);
        assertFalse(not_exists);
    }

    @Test
    public void testFindByIdWithBooks() {
        Optional<Category> optionalCategory = categoryRepository.findByIdWithBooks(testBook.getId());
        Category category = null;
        if(optionalCategory.isPresent()){
            category = optionalCategory.get();
        }
        assertNotNull(category);
        assertThat(category.getName()).isEqualTo("Fiction");
        assertThat(category.getDescription()).isEqualTo("Fiction books");
    }

    @Test
    public void testFindAllWithBookCount() {
        List<Object[]> count = categoryRepository.findAllWithBookCount();
        assertThat(count.size() == 1L);
//        assertThat(count.get(0)).
    }

    @Test
    public void testFindCategoriesWithBooks() {
    }

    @Test
    public void testFindCategoriesWithoutBooks() {
    }
}