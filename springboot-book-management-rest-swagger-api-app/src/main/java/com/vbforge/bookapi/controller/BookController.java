package com.vbforge.bookapi.controller;

import com.vbforge.bookapi.dto.BookCreateDTO;
import com.vbforge.bookapi.dto.BookDTO;
import com.vbforge.bookapi.dto.BookUpdateDTO;
import com.vbforge.bookapi.dto.response.ApiResponse;
import com.vbforge.bookapi.dto.response.PageResponse;
import com.vbforge.bookapi.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for Book management
 * Base path: /api/books
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Books", description = "Book management APIs")
public class BookController {

    private final BookService bookService;

    //CREATE
    @PostMapping
    @Operation(summary = "Create a new book", description = "Creates a new book in the system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Book created successfully",
                    content = @Content(schema = @Schema(implementation = BookDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data"
            )
    })
    public ResponseEntity<ApiResponse<BookDTO>> createBook(@Valid @RequestBody BookCreateDTO bookCreateDTO){
        log.info("REST request to create book: {}", bookCreateDTO.getTitle());

        BookDTO createdBook = bookService.createBook(bookCreateDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book created successfully", createdBook));
    }

    //READ
    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Returns a single book by its ID")
    public ResponseEntity<ApiResponse<BookDTO>> getBookById(@Parameter (description = "Book ID") @PathVariable Long id){
        log.info("REST request to get book by ID: {}", id);

        BookDTO book = bookService.getBookById(id);

        return ResponseEntity.ok(ApiResponse.success(book));
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "Get book by ISBN", description = "Returns a single book by its ISBN")
    public ResponseEntity<ApiResponse<BookDTO>> getBookByIsbn(@Parameter(description = "Book ISBN") @PathVariable String isbn) {
        log.info("REST request to get book by ISBN: {}", isbn);

        BookDTO book = bookService.getBookByIsbn(isbn);

        return ResponseEntity.ok(ApiResponse.success(book));
    }

    @GetMapping
    @Operation(summary = "Get all books", description = "Returns all books with pagination and sorting")
    public ResponseEntity<ApiResponse<PageResponse<BookDTO>>> getAllBooks(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "ASC") String direction) {
        log.info("REST request to get all books - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, direction);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<BookDTO> booksPage = bookService.getAllBooks(pageable);
        PageResponse<BookDTO> response = PageResponse.from(booksPage);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    //UPDATE
    @PutMapping("/{id}")
    @Operation(summary = "Update book", description = "Updates an existing book")
    public ResponseEntity<ApiResponse<BookDTO>> updateBook(
            @Parameter(description = "Book ID") @PathVariable Long id,
            @Valid @RequestBody BookUpdateDTO bookUpdateDTO) {
        log.info("REST request to update book with ID: {}", id);

        BookDTO updatedBook = bookService.updateBook(id, bookUpdateDTO);

        return ResponseEntity.ok(ApiResponse.success("Book updated successfully", updatedBook));
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Update book stock", description = "Updates the stock quantity of a book")
    public ResponseEntity<ApiResponse<BookDTO>> updateStock(
            @Parameter(description = "Book ID") @PathVariable Long id,
            @Parameter(description = "New stock quantity") @RequestParam Integer quantity) {
        log.info("REST request to update stock for book ID: {} to quantity: {}", id, quantity);

        BookDTO updatedBook = bookService.updateStockQuantity(id, quantity);

        return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", updatedBook));
    }

    //DELETE
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete book", description = "Deletes a book from the system")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@Parameter(description = "Book ID") @PathVariable Long id) {
        log.info("REST request to delete book with ID: {}", id);

        bookService.deleteBook(id);

        return ResponseEntity.ok(ApiResponse.success("Book deleted successfully", null));
    }

    //SEARCH & FILTER
    @GetMapping("/search")
    @Operation(summary = "Search books", description = "Search books by keyword in title, description, or author name")
    public ResponseEntity<ApiResponse<PageResponse<BookDTO>>> searchBooks(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to search books with keyword: {}", keyword);

        Pageable pageable = PageRequest.of(page, size);
        Page<BookDTO> booksPage = bookService.searchBooks(keyword, pageable);
        PageResponse<BookDTO> response = PageResponse.from(booksPage);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get books by category", description = "Returns all books in a specific category")
    public ResponseEntity<ApiResponse<PageResponse<BookDTO>>> getBooksByCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to get books by category ID: {}", categoryId);

        Pageable pageable = PageRequest.of(page, size);
        Page<BookDTO> booksPage = bookService.getBooksByCategory(categoryId, pageable);
        PageResponse<BookDTO> response = PageResponse.from(booksPage);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/publisher/{publisherId}")
    @Operation(summary = "Get books by publisher", description = "Returns all books from a specific publisher")
    public ResponseEntity<ApiResponse<List<BookDTO>>> getBooksByPublisher(
            @Parameter(description = "Publisher ID") @PathVariable Long publisherId) {
        log.info("REST request to get books by publisher ID: {}", publisherId);

        List<BookDTO> books = bookService.getBooksByPublisher(publisherId);

        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @GetMapping("/author/{authorId}")
    @Operation(summary = "Get books by author", description = "Returns all books by a specific author")
    public ResponseEntity<ApiResponse<List<BookDTO>>> getBooksByAuthor(
            @Parameter(description = "Author ID") @PathVariable Long authorId) {
        log.info("REST request to get books by author ID: {}", authorId);

        List<BookDTO> books = bookService.getBooksByAuthor(authorId);

        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get books by price range", description = "Returns books within a price range")
    public ResponseEntity<ApiResponse<List<BookDTO>>> getBooksByPriceRange(
            @Parameter(description = "Minimum price") @RequestParam BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam BigDecimal maxPrice) {
        log.info("REST request to get books by price range: {} - {}", minPrice, maxPrice);

        List<BookDTO> books = bookService.getBooksByPriceRange(minPrice, maxPrice);

        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @GetMapping("/in-stock")
    @Operation(summary = "Get books in stock", description = "Returns all books that are currently in stock")
    public ResponseEntity<ApiResponse<List<BookDTO>>> getBooksInStock() {
        log.info("REST request to get books in stock");

        List<BookDTO> books = bookService.getBooksInStock();

        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get books with low stock", description = "Returns books with stock below threshold")
    public ResponseEntity<ApiResponse<List<BookDTO>>> getBooksWithLowStock(
            @Parameter(description = "Stock threshold") @RequestParam(defaultValue = "10") Integer threshold) {
        log.info("REST request to get books with low stock (threshold: {})", threshold);

        List<BookDTO> books = bookService.getBooksWithLowStock(threshold);

        return ResponseEntity.ok(ApiResponse.success(books));
    }

}
