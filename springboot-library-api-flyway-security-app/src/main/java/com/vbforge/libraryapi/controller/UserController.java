package com.vbforge.libraryapi.controller;

import com.vbforge.libraryapi.dto.response.BookResponse;
import com.vbforge.libraryapi.dto.response.BorrowResponse;
import com.vbforge.libraryapi.exception.custom.BadRequestException;
import com.vbforge.libraryapi.service.BookService;
import com.vbforge.libraryapi.service.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final BorrowService borrowService;
    private final BookService bookService;


    @PostMapping("/books/borrow/{bookId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<BorrowResponse> borrowBook(@PathVariable Long bookId) {
        if (bookId == null || bookId <= 0) {
            throw new BadRequestException("Book ID must be a positive number");
        }
        log.info("User borrowing book with ID: {}", bookId);
        return ResponseEntity.ok(borrowService.borrowBook(bookId));
    }

    @PostMapping("/books/return/{bookId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<BorrowResponse> returnBook(@PathVariable Long bookId) {
        log.info("User returning book with ID: {}", bookId);
        return ResponseEntity.ok(borrowService.returnBook(bookId));
    }

    @GetMapping("/records")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<BorrowResponse>> getBorrowRecords() {
        log.info("Fetching user's borrow records");
        return ResponseEntity.ok(borrowService.getUserBorrowRecords());
    }

    @GetMapping("/books/available")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<BookResponse>> getAvailableBooks() {
        log.info("Fetching available books");
        return ResponseEntity.ok(bookService.getAvailableBooks());
    }

}