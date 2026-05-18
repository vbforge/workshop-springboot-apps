package com.vbforge.bookapi.repository;

import com.vbforge.bookapi.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Book entity
 * Includes pagination, sorting, and custom queries
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long>{

    // ========================================
    // Basic Queries (Method Name Queries)
    // ========================================

    /**
     * Find book by ISBN (unique identifier)
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Find books by title (exact match)
     */
    List<Book> findByTitle(String title);

    /**
     * Find books by title containing (case-insensitive)
     */
    List<Book> findByTitleContainingIgnoreCase(String title);

    /**
     * Find books by language
     */
    List<Book> findByLanguage(String language);

    /**
     * Find books by category ID
     */
    List<Book> findByCategoryId(Long categoryId);

    /**
     * Find books by publisher ID
     */
    List<Book> findByPublisherId(Long publisherId);

    /**
     * Check if book exists by ISBN
     */
    boolean existsByIsbn(String isbn);

    // ========================================
    // Price-based Queries
    // ========================================

    /**
     * Find books by price less than or equal
     */
    List<Book> findByPriceLessThanEqual(BigDecimal price);

    /**
     * Find books by price between range
     */
    List<Book> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find books ordered by price ascending
     */
    List<Book> findAllByOrderByPriceAsc();

    /**
     * Find books ordered by price descending
     */
    List<Book> findAllByOrderByPriceDesc();

    // ========================================
    // Stock-based Queries
    // ========================================

    /**
     * Find books in stock (quantity > 0)
     */
    @Query("SELECT b FROM Book b WHERE b.stockQuantity > 0")
    List<Book> findBooksInStock();

    /**
     * Find books out of stock (quantity = 0)
     */
    @Query("SELECT b FROM Book b WHERE b.stockQuantity = 0")
    List<Book> findBooksOutOfStock();

    /**
     * Find books with low stock (quantity <= threshold)
     */
    @Query("SELECT b FROM Book b WHERE b.stockQuantity <= :threshold AND b.stockQuantity > 0")
    List<Book> findBooksWithLowStock(@Param("threshold") Integer threshold);

    // ========================================
    // Date-based Queries
    // ========================================

    /**
     * Find books published after a certain date
     */
    List<Book> findByPublicationDateAfter(LocalDate date);

    /**
     * Find books published between dates
     */
    List<Book> findByPublicationDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find recently published books (within last N days)
     */
    @Query("SELECT b FROM Book b WHERE b.publicationDate >= :date ORDER BY b.publicationDate DESC")
    List<Book> findRecentlyPublished(@Param("date") LocalDate date);

    // ========================================
    // Relationship-based Queries (with JOIN FETCH)
    // ========================================

    /**
     * Find book by ID with all relationships loaded (authors, category, publisher)
     * JOIN FETCH for eager loading to avoid N+1 query problem
     */
    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN FETCH b.authors " +
            "LEFT JOIN FETCH b.category " +
            "LEFT JOIN FETCH b.publisher " +
            "WHERE b.id = :id")
    Optional<Book> findByIdWithDetails(@Param("id") Long id);

    /**
     * Find all books with all relationships loaded
     */
    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN FETCH b.authors " +
            "LEFT JOIN FETCH b.category " +
            "LEFT JOIN FETCH b.publisher")
    List<Book> findAllWithDetails();

    /**
     * Find books by author ID
     */
    @Query("SELECT DISTINCT b FROM Book b JOIN b.authors a WHERE a.id = :authorId")
    List<Book> findByAuthorId(@Param("authorId") Long authorId);

    /**
     * Find books by author name (case-insensitive)
     */
    @Query("SELECT DISTINCT b FROM Book b JOIN b.authors a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    List<Book> findByAuthorNameContaining(@Param("authorName") String authorName);

    /**
     * Find books by category name
     */
    @Query("SELECT b FROM Book b WHERE b.category.name = :categoryName")
    List<Book> findByCategoryName(@Param("categoryName") String categoryName);

    /**
     * Find books by publisher name
     */
    @Query("SELECT b FROM Book b WHERE b.publisher.name = :publisherName")
    List<Book> findByPublisherName(@Param("publisherName") String publisherName);

    // ========================================
    // Pagination & Sorting
    // ========================================

    /**
     * Find all books with pagination
     */
    Page<Book> findAll(Pageable pageable);

    /**
     * Find books by category with pagination
     */
    Page<Book> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * Find books by title containing with pagination
     */
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // ========================================
    // Search Queries (Multi-criteria)
    // ========================================

    /**
     * Search books by multiple criteria
     * Searches in title, description, and author name
     */
    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN b.authors a " +
            "WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Book> searchBooks(@Param("searchTerm") String searchTerm);

    /**
     * Search books with pagination
     */
    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN b.authors a " +
            "WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Book> searchBooks(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ========================================
    // Statistical Queries
    // ========================================

    /**
     * Count books by category
     */
    @Query("SELECT COUNT(b) FROM Book b WHERE b.category.id = :categoryId")
    Long countByCategory(@Param("categoryId") Long categoryId);

    /**
     * Count books by publisher
     */
    @Query("SELECT COUNT(b) FROM Book b WHERE b.publisher.id = :publisherId")
    Long countByPublisher(@Param("publisherId") Long publisherId);

    /**
     * Count total books in stock
     */
    @Query("SELECT SUM(b.stockQuantity) FROM Book b")
    Long countTotalStockQuantity();

    /**
     * Get average book price
     */
    @Query("SELECT AVG(b.price) FROM Book b")
    BigDecimal getAveragePrice();

    /**
     * Find most expensive books (top N)
     */
    @Query("SELECT b FROM Book b ORDER BY b.price DESC")
    List<Book> findMostExpensive(Pageable pageable);

    /**
     * Find the cheapest books (top N)
     */
    @Query("SELECT b FROM Book b ORDER BY b.price ASC")
    List<Book> findCheapest(Pageable pageable);

}
