package com.vbforge.wookie.controller;

import com.vbforge.wookie.dto.request.BookCreateRequest;
import com.vbforge.wookie.dto.request.BookUpdateRequest;
import com.vbforge.wookie.dto.response.BookResponse;
import com.vbforge.wookie.service.BookService;
import com.vbforge.wookie.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/wookie_books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * Create a new book (authenticated users only, RESTRICTED_USER excluded)
     * POST /api/wookie_books
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookCreateRequest request) {
        String currentUser = SecurityUtils.getCurrentUser().getAuthorPseudonym();
        log.info("Creating book: {} by user: {}", request.getTitle(), currentUser);
        
        BookResponse response = bookService.createBook(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get book by ID (public)
     * GET /api/wookie_books/{bookId}
     */
    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long bookId) {
        log.debug("Fetching book by ID: {}", bookId);
        
        return bookService.findBookById(bookId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search books with filters (public, paginated)
     * GET /api/wookie_books?authorPseudonym=&title=&minPrice=&maxPrice=&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<BookResponse>> searchBooks(
            @RequestParam(required = false) String authorPseudonym,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.debug("Searching books with filters - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<BookResponse> books = bookService.searchBooks(
                authorPseudonym, title, minPrice, maxPrice, pageable);
        
        return ResponseEntity.ok(books);
    }

    /**
     * Get all books (admin only)
     * GET /api/wookie_books/admin/all
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        log.info("Fetching all books (admin request)");
        return ResponseEntity.ok(bookService.findAllBooks());
    }

    /**
     * Get current user's books
     * GET /api/wookie_books/me/books
     */
    @GetMapping("/me/books")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookResponse>> getMyBooks() {
        String currentUser = SecurityUtils.getCurrentUser().getAuthorPseudonym();
        log.debug("Fetching books for user: {}", currentUser);
        
        List<BookResponse> books = bookService.findMyBooks(currentUser);
        return ResponseEntity.ok(books);
    }

    /**
     * Update a book (owner or admin only)
     * PUT /api/wookie_books/{bookId}
     */
    @PutMapping("/{bookId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long bookId,
            @Valid @RequestBody BookUpdateRequest request) {
        
        String currentUser = SecurityUtils.getCurrentUser().getAuthorPseudonym();
        log.info("Updating book ID: {} by user: {}", bookId, currentUser);
        
        BookResponse response = bookService.updateBook(bookId, request, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete (unpublish) a book (owner or admin only)
     * DELETE /api/wookie_books?title=BookTitle
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteBook(@RequestParam String title) {
        String currentUser = SecurityUtils.getCurrentUser().getAuthorPseudonym();
        log.info("Deleting book with title: {} by user: {}", title, currentUser);
        
        bookService.deleteBook(title, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get book by title (public)
     * GET /api/wookie_books/title/{title}
     */
    @GetMapping("/title/{title}")
    public ResponseEntity<BookResponse> getBookByTitle(@PathVariable String title) {
        log.debug("Fetching book by title: {}", title);
        
        return bookService.findBookByTitle(title)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Toggle publish status (unpublish/republish) - owner or admin only
     * PATCH /api/wookie_books/{bookId}/publish?publish=true|false
     */
    @PatchMapping("/{bookId}/publish")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookResponse> togglePublishStatus(
            @PathVariable Long bookId,
            @RequestParam boolean publish) {

        String currentUser = SecurityUtils.getCurrentUser().getAuthorPseudonym();
        log.info("{} book ID: {} by user: {}",
                publish ? "Publishing" : "Unpublishing",
                bookId,
                currentUser);

        BookResponse response = bookService.togglePublishStatus(bookId, publish, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Hard delete a book (SUPER_ADMIN only)
     * DELETE /api/wookie_books/admin/{bookId}
     */
    @DeleteMapping("/admin/{bookId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> hardDeleteBook(@PathVariable Long bookId) {
        String currentUser = SecurityUtils.getCurrentUser().getAuthorPseudonym();
        log.warn("Admin hard delete request for book ID: {} by user: {}", bookId, currentUser);

        bookService.hardDeleteBook(bookId, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all unpublished books (SUPER_ADMIN only)
     * GET /api/wookie_books/admin/unpublished
     */
    @GetMapping("/admin/unpublished")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<BookResponse>> getUnpublishedBooks() {
        log.info("Fetching all unpublished books (admin request)");
        return ResponseEntity.ok(bookService.findAllUnpublishedBooks());
    }

    /**
     * Get soft-deleted (unpublished) books older than specified days (SUPER_ADMIN only)
     * GET /api/wookie_books/admin/cleanup-candidates?daysOld=90
     */
    @GetMapping("/admin/cleanup-candidates")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<BookResponse>> getCleanupCandidates(
            @RequestParam(defaultValue = "90") int daysOld) {

        log.info("Fetching books unpublished for more than {} days", daysOld);

        List<BookResponse> oldUnpublishedBooks = bookService.findAllUnpublishedBooks().stream()
                .filter(book -> book.getUpdatedAt() != null &&
                        book.getUpdatedAt().isBefore(java.time.LocalDateTime.now().minusDays(daysOld)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(oldUnpublishedBooks);
    }

}