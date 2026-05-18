package com.vbforge.booksapi.controller;

import com.vbforge.booksapi.dto.request.BookRequestDTO;
import com.vbforge.booksapi.dto.response.BookResponseDTO;
import com.vbforge.booksapi.entity.Genre;
import com.vbforge.booksapi.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;

    // ===== BASIC CRUD ENDPOINTS =====

    @GetMapping
    public ResponseEntity<List<BookResponseDTO>> getAllBooks() {
        log.info("REST request to get all books");
        List<BookResponseDTO> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getBookById(@PathVariable Long id) {
        log.info("REST request to get book by id: {}", id);
        BookResponseDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @PostMapping
    public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody BookRequestDTO bookRequestDTO) {
        log.info("REST request to create new book: {}", bookRequestDTO.getTitle());
        BookResponseDTO createdBook = bookService.insertBook(bookRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDTO> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequestDTO bookRequestDTO) {
        log.info("REST request to update book with id: {}", id);
        bookService.updateBookById(id, bookRequestDTO);
        BookResponseDTO updatedBook = bookService.getBookById(id);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        log.info("REST request to delete book with id: {}", id);
        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }

    // ===== ADVANCED QUERY ENDPOINTS =====

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkBookExists(
            @RequestParam String title,
            @RequestParam String author) {
        log.info("REST request to check if book exists - title: '{}', author: '{}'", title, author);
        boolean exists = bookService.existsByTitleAndAuthor(title, author);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<BookResponseDTO>> getBooksByGenre(@PathVariable Genre genre) {
        log.info("REST request to get books by genre: {}", genre);
        List<BookResponseDTO> books = bookService.getBooksByGenre(genre);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/genre/{genre}/count")
    public ResponseEntity<Long> getBookCountByGenre(@PathVariable Genre genre) {
        log.info("REST request to get book count by genre: {}", genre);
        long count = bookService.getBookCountByGenre(genre);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookResponseDTO>> searchBooks(@RequestParam String keyword) {
        log.info("REST request to search books with keyword: '{}'", keyword);
        List<BookResponseDTO> books = bookService.searchBooks(keyword);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/advanced-search")
    public ResponseEntity<List<BookResponseDTO>> advancedSearch(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author) {
        log.info("REST request for advanced search - title: '{}', author: '{}'", title, author);
        List<BookResponseDTO> books = bookService.searchBooksByTitleOrAuthor(title, author);
        return ResponseEntity.ok(books);
    }
}