package com.vbforge.booksapi.repository;

import com.vbforge.booksapi.entity.Book;
import com.vbforge.booksapi.entity.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    private Book book1;
    private Book book2;
    private Book book3;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();

        book1 = Book.builder()
                .title("The Pragmatic Programmer")
                .description("A classic software development book")
                .author("David Thomas")
                .genre(Genre.Education)
                .build();

        book2 = Book.builder()
                .title("Harry Potter and the Sorcerer's Stone")
                .description("A magical fantasy adventure")
                .author("J.K. Rowling")
                .genre(Genre.Fantasy)
                .build();

        book3 = Book.builder()
                .title("The Hobbit")
                .description("A fantasy novel about Bilbo Baggins")
                .author("J.R.R. Tolkien")
                .genre(Genre.Fantasy)
                .build();

        bookRepository.save(book1);
        bookRepository.save(book2);
        bookRepository.save(book3);
    }

    // ===== TEST 1: existsByTitleIgnoreCaseAndAuthorIgnoreCase =====
    
    @Test
    void existsByTitleIgnoreCaseAndAuthorIgnoreCase_ShouldReturnTrue_WhenBookExists() {
        boolean exists = bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase(
                "the pragmatic programmer", "david thomas");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByTitleIgnoreCaseAndAuthorIgnoreCase_ShouldReturnTrue_WhenTitleCaseDiffers() {
        boolean exists = bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase(
                "THE PRAGMATIC PROGRAMMER", "DAVID THOMAS");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByTitleIgnoreCaseAndAuthorIgnoreCase_ShouldReturnFalse_WhenBookDoesNotExist() {
        boolean exists = bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase(
                "Nonexistent Book", "Unknown Author");
        assertThat(exists).isFalse();
    }

    @Test
    void existsByTitleIgnoreCaseAndAuthorIgnoreCase_ShouldReturnFalse_WhenTitleExistsButAuthorDifferent() {
        boolean exists = bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase(
                "The Pragmatic Programmer", "Wrong Author");
        assertThat(exists).isFalse();
    }

    // ===== TEST 2: findByGenre =====
    
    @Test
    void findByGenre_ShouldReturnAllBooksOfGivenGenre() {
        List<Book> fantasyBooks = bookRepository.findByGenre(Genre.Fantasy);
        
        assertThat(fantasyBooks).hasSize(2);
        assertThat(fantasyBooks).extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Harry Potter and the Sorcerer's Stone", "The Hobbit");
    }

    @Test
    void findByGenre_ShouldReturnEmptyList_WhenNoBooksOfGenreExist() {
        List<Book> romanceBooks = bookRepository.findByGenre(Genre.Romance);
        assertThat(romanceBooks).isEmpty();
    }

    @Test
    void findByGenre_ShouldReturnSingleBook_WhenOnlyOneExists() {
        List<Book> educationBooks = bookRepository.findByGenre(Genre.Education);
        
        assertThat(educationBooks).hasSize(1);
        assertThat(educationBooks.get(0).getTitle()).isEqualTo("The Pragmatic Programmer");
    }

    // ===== TEST 3: Case-Insensitive Search with AND logic =====
    
    @Test
    void searchBooksCaseInsensitive_ShouldFindBooksByTitleKeyword_CaseInsensitive() {
        // Search by title only (author is null)
        List<Book> books = bookRepository.searchBooksCaseInsensitive("HARRY", null);
        
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("Harry Potter and the Sorcerer's Stone");
    }

    @Test
    void searchBooksCaseInsensitive_ShouldFindBooksByAuthorKeyword_CaseInsensitive() {
        // Search by author only (title is null)
        List<Book> books = bookRepository.searchBooksCaseInsensitive(null, "rowling");
        
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getAuthor()).isEqualTo("J.K. Rowling");
    }

    @Test
    void searchBooksCaseInsensitive_ShouldFindBooksByTitleAndAuthor() {
        // Search by both title and author
        List<Book> books = bookRepository.searchBooksCaseInsensitive("pragmatic", "thomas");
        
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("The Pragmatic Programmer");
    }

    @Test
    void searchBooksCaseInsensitive_ShouldBeCaseInsensitive() {
        // Test with UPPERCASE
        List<Book> books1 = bookRepository.searchBooksCaseInsensitive("PRAGMATIC", "THOMAS");
        assertThat(books1).hasSize(1);
        assertThat(books1.get(0).getTitle()).isEqualTo("The Pragmatic Programmer");
        
        // Test with lowercase
        List<Book> books2 = bookRepository.searchBooksCaseInsensitive("pragmatic", "thomas");
        assertThat(books2).hasSize(1);
        
        // Test with mixed case
        List<Book> books3 = bookRepository.searchBooksCaseInsensitive("PrAgMaTiC", "ThOmAs");
        assertThat(books3).hasSize(1);
    }

    @Test
    void searchBooksCaseInsensitive_ShouldReturnEmptyList_WhenNoMatchFound() {
        List<Book> books = bookRepository.searchBooksCaseInsensitive("Nonexistent", "Unknown");
        assertThat(books).isEmpty();
    }

    @Test
    void searchBooksCaseInsensitive_ShouldReturnAllBooks_WhenBothParametersAreNull() {
        List<Book> books = bookRepository.searchBooksCaseInsensitive(null, null);
        assertThat(books).hasSize(3);
    }

    @Test
    void searchBooksCaseInsensitive_ShouldReturnAllBooks_WhenBothParametersAreEmpty() {
        List<Book> books = bookRepository.searchBooksCaseInsensitive("", "");
        assertThat(books).hasSize(3);
    }

    // ===== TEST 4: searchByKeyword (OR logic for single keyword) =====
    
    @Test
    void searchByKeyword_ShouldFindBooksByTitleKeyword() {
        List<Book> books = bookRepository.searchByKeyword("Harry");
        
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).contains("Harry Potter");
    }

    @Test
    void searchByKeyword_ShouldFindBooksByAuthorKeyword() {
        List<Book> books = bookRepository.searchByKeyword("Rowling");
        
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getAuthor()).isEqualTo("J.K. Rowling");
    }

    @Test
    void searchByKeyword_ShouldFindBooksByTitleOrAuthor() {
        List<Book> books = bookRepository.searchByKeyword("Hobbit");
        
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("The Hobbit");
    }

    @Test
    void searchByKeyword_ShouldBeCaseInsensitive() {
        List<Book> books = bookRepository.searchByKeyword("PRAGMATIC");
        
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("The Pragmatic Programmer");
    }

    @Test
    void searchByKeyword_ShouldReturnAllBooks_WhenKeywordIsNull() {
        List<Book> books = bookRepository.searchByKeyword(null);
        assertThat(books).hasSize(3);
    }

    @Test
    void searchByKeyword_ShouldReturnAllBooks_WhenKeywordIsEmpty() {
        List<Book> books = bookRepository.searchByKeyword("");
        assertThat(books).hasSize(3);
    }

    // ===== TEST 5: JpaRepository Built-in Methods =====
    
    @Test
    void findAll_ShouldReturnAllBooks() {
        List<Book> allBooks = bookRepository.findAll();
        assertThat(allBooks).hasSize(3);
    }

    @Test
    void findById_ShouldReturnBook_WhenIdExists() {
        Book foundBook = bookRepository.findById(book1.getId()).orElse(null);
        
        assertThat(foundBook).isNotNull();
        assertThat(foundBook.getTitle()).isEqualTo(book1.getTitle());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        Book foundBook = bookRepository.findById(999L).orElse(null);
        assertThat(foundBook).isNull();
    }

    @Test
    void deleteById_ShouldDeleteBook() {
        assertThat(bookRepository.findById(book1.getId())).isPresent();
        
        bookRepository.deleteById(book1.getId());
        
        assertThat(bookRepository.findById(book1.getId())).isNotPresent();
        assertThat(bookRepository.findAll()).hasSize(2);
    }

    @Test
    void save_ShouldCreateNewBook() {
        Book newBook = Book.builder()
                .title("Clean Code")
                .description("A handbook of agile software craftsmanship")
                .author("Robert C. Martin")
                .genre(Genre.Education)
                .build();
        
        Book savedBook = bookRepository.save(newBook);
        
        assertThat(savedBook.getId()).isNotNull();
        assertThat(bookRepository.findAll()).hasSize(4);
    }
}