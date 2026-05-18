package com.vbforge.bookapi.service;

import com.vbforge.bookapi.dto.BookCreateDTO;
import com.vbforge.bookapi.dto.BookDTO;
import com.vbforge.bookapi.dto.BookUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for Book business logic
 */
public interface BookService {

    /**
     * Create a new book
     */
    BookDTO createBook(BookCreateDTO bookCreateDTO);

    /**
     * Get book by ID
     */
    BookDTO getBookById(Long id);

    /**
     * Get book by ISBN
     */
    BookDTO getBookByIsbn(String isbn);

    /**
     * Get all books with pagination
     */
    Page<BookDTO> getAllBooks(Pageable pageable);

    /**
     * Get all books without pagination
     */
    List<BookDTO> getAllBooks();

    /**
     * Update book
     */
    BookDTO updateBook(Long id, BookUpdateDTO bookUpdateDTO);

    /**
     * Delete book
     */
    void deleteBook(Long id);

    /**
     * Search books by keyword (title, description, author name)
     */
    Page<BookDTO> searchBooks(String keyword, Pageable pageable);

    /**
     * Find books by category
     */
    Page<BookDTO> getBooksByCategory(Long categoryId, Pageable pageable);

    /**
     * Find books by publisher
     */
    List<BookDTO> getBooksByPublisher(Long publisherId);

    /**
     * Find books by author
     */
    List<BookDTO> getBooksByAuthor(Long authorId);

    /**
     * Find books by price range
     */
    List<BookDTO> getBooksByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find books in stock
     */
    List<BookDTO> getBooksInStock();

    /**
     * Find books with low stock
     */
    List<BookDTO> getBooksWithLowStock(Integer threshold);

    /**
     * Update stock quantity
     */
    BookDTO updateStockQuantity(Long id, Integer quantity);

}
