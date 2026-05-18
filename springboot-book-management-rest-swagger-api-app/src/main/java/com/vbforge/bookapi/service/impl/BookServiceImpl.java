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
import com.vbforge.bookapi.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of BookService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final BookMapper bookMapper;

    @Override
    @Transactional
    public BookDTO createBook(BookCreateDTO bookCreateDTO) {
        log.info("Creating new book with ISBN: {}", bookCreateDTO.getIsbn());

        // Validate ISBN uniqueness
        if (bookRepository.existsByIsbn(bookCreateDTO.getIsbn())) {
            throw new DuplicateResourceException("Book", "ISBN", bookCreateDTO.getIsbn());
        }

        // Map DTO to entity
        Book book = bookMapper.toEntity(bookCreateDTO);

        // Set relationships
        setBookRelationships(book, bookCreateDTO.getCategoryId(), bookCreateDTO.getPublisherId(), bookCreateDTO.getAuthorIds());

        // Save book
        Book savedBook = bookRepository.save(book);
        log.info("Book created successfully with ID: {}", savedBook.getId());

        return bookMapper.toDTO(savedBook);
    }

    @Override
    public BookDTO getBookById(Long id) {
        log.debug("Fetching book with ID: {}", id);

        Book book = bookRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));

        return bookMapper.toDTO(book);
    }

    @Override
    public BookDTO getBookByIsbn(String isbn) {
        log.debug("Fetching book with ISBN: {}", isbn);

        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ISBN: " + isbn));

        return bookMapper.toDTO(book);
    }

    @Override
    public Page<BookDTO> getAllBooks(Pageable pageable) {
        log.debug("Fetching all books with pagination: {}", pageable);

        return bookRepository.findAll(pageable)
                .map(bookMapper::toDTO);
    }

    @Override
    public List<BookDTO> getAllBooks() {
        log.debug("Fetching all books without pagination");

        List<Book> books = bookRepository.findAllWithDetails();
        return bookMapper.toDTOList(books);
    }

    @Override
    @Transactional
    public BookDTO updateBook(Long id, BookUpdateDTO bookUpdateDTO) {
        log.info("Updating book with ID: {}", id);

        // Find existing book
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));

        // Update fields from DTO (only non-null fields)
        bookMapper.updateEntityFromDTO(bookUpdateDTO, book);

        // Update relationships if provided
        if (bookUpdateDTO.getCategoryId() != null || bookUpdateDTO.getPublisherId() != null || bookUpdateDTO.getAuthorIds() != null) {
            setBookRelationships(book, bookUpdateDTO.getCategoryId(), bookUpdateDTO.getPublisherId(), bookUpdateDTO.getAuthorIds());
        }

        // Save updated book
        Book updatedBook = bookRepository.save(book);
        log.info("Book updated successfully with ID: {}", updatedBook.getId());

        return bookMapper.toDTO(updatedBook);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        log.info("Deleting book with ID: {}", id);

        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with ID: " + id);
        }

        bookRepository.deleteById(id);
        log.info("Book deleted successfully with ID: {}", id);
    }

    @Override
    public Page<BookDTO> searchBooks(String keyword, Pageable pageable) {
        log.debug("Searching books with keyword: {}", keyword);

        return bookRepository.searchBooks(keyword, pageable)
                .map(bookMapper::toDTO);
    }

    @Override
    public Page<BookDTO> getBooksByCategory(Long categoryId, Pageable pageable) {
        log.debug("Fetching books by category ID: {}", categoryId);

        // Validate category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with ID: " + categoryId);
        }

        return bookRepository.findByCategoryId(categoryId, pageable)
                .map(bookMapper::toDTO);
    }

    @Override
    public List<BookDTO> getBooksByPublisher(Long publisherId) {
        log.debug("Fetching books by publisher ID: {}", publisherId);

        // Validate publisher exists
        if (!publisherRepository.existsById(publisherId)) {
            throw new ResourceNotFoundException("Publisher not found with ID: " + publisherId);
        }

        List<Book> books = bookRepository.findByPublisherId(publisherId);
        return bookMapper.toDTOList(books);
    }

    @Override
    public List<BookDTO> getBooksByAuthor(Long authorId) {
        log.debug("Fetching books by author ID: {}", authorId);

        // Validate author exists
        if (!authorRepository.existsById(authorId)) {
            throw new ResourceNotFoundException("Author not found with ID: " + authorId);
        }

        List<Book> books = bookRepository.findByAuthorId(authorId);
        return bookMapper.toDTOList(books);
    }

    @Override
    public List<BookDTO> getBooksByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Fetching books by price range: {} - {}", minPrice, maxPrice);

        if (minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidOperationException("Min price cannot be greater than max price");
        }

        List<Book> books = bookRepository.findByPriceBetween(minPrice, maxPrice);
        return bookMapper.toDTOList(books);
    }

    @Override
    public List<BookDTO> getBooksInStock() {
        log.debug("Fetching books in stock");

        List<Book> books = bookRepository.findBooksInStock();
        return bookMapper.toDTOList(books);
    }

    @Override
    public List<BookDTO> getBooksWithLowStock(Integer threshold) {
        log.debug("Fetching books with low stock (threshold: {})", threshold);

        if (threshold < 0) {
            throw new InvalidOperationException("Threshold cannot be negative");
        }

        List<Book> books = bookRepository.findBooksWithLowStock(threshold);
        return bookMapper.toDTOList(books);
    }

    @Override
    @Transactional
    public BookDTO updateStockQuantity(Long id, Integer quantity) {
        log.info("Updating stock quantity for book ID: {} to {}", id, quantity);

        if (quantity < 0) {
            throw new InvalidOperationException("Stock quantity cannot be negative");
        }

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));

        book.setStockQuantity(quantity);
        Book updatedBook = bookRepository.save(book);

        log.info("Stock quantity updated successfully for book ID: {}", id);
        return bookMapper.toDTO(updatedBook);
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Set book relationships (category, publisher, authors)
     */
    private void setBookRelationships(Book book, Long categoryId, Long publisherId, Set<Long> authorIds) {
        // Set category
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));
            book.setCategory(category);
        }

        // Set publisher
        if (publisherId != null) {
            Publisher publisher = publisherRepository.findById(publisherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Publisher not found with ID: " + publisherId));
            book.setPublisher(publisher);
        }

        // Set authors (iterating through Set)
        if (authorIds != null && !authorIds.isEmpty()) {
            Set<Author> authors = new HashSet<>();
            for (Long authorId : authorIds) {
                Author author = authorRepository.findById(authorId)
                        .orElseThrow(() -> new ResourceNotFoundException("Author not found with ID: " + authorId));
                authors.add(author);
            }
            book.setAuthors(authors);
        }
    }

    /**
     * Custom exception for resource not found
     *  (were used for previous development steps, before exception implemented)
     *  (it leaves commented because we use 'exception' for handling package; this one as example from previous)
     */
//    public static class ResourceNotFoundException extends RuntimeException {
//        public ResourceNotFoundException(String message) {
//            super(message);
//        }
//    }

}












