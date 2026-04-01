package com.vbforge.libraryapi.controller;

import com.vbforge.libraryapi.dto.request.BookRequest;
import com.vbforge.libraryapi.dto.request.UpdateRoleRequest;
import com.vbforge.libraryapi.dto.request.UpdateUserRequest;
import com.vbforge.libraryapi.dto.response.BookResponse;
import com.vbforge.libraryapi.dto.response.UserResponse;
import com.vbforge.libraryapi.entity.Role;
import com.vbforge.libraryapi.exception.custom.BadRequestException;
import com.vbforge.libraryapi.service.impl.BookServiceImpl;
import com.vbforge.libraryapi.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/librarian")
@RequiredArgsConstructor
@Slf4j
public class LibrarianController {

    private final BookServiceImpl bookService;
    private final UserServiceImpl userService;

    //========== Books management ==========

    @PostMapping("/books")
    @PreAuthorize("hasAuthority('LIBRARIAN')")
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest bookRequest) {
        log.info("Creating book: {}", bookRequest.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.addBook(bookRequest));
    }

    @GetMapping("/books")
    @PreAuthorize("hasAuthority('LIBRARIAN')")
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size ) {
        Pageable pageable = PageRequest.of(page, size);
        log.info("Fetching all books (page: {}, size: {})", page, size);
        return ResponseEntity.ok(bookService.findAllBooks(pageable));
    }

    @GetMapping("/books/{id}")
    @PreAuthorize("hasAuthority('LIBRARIAN')")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        log.info("Fetching book by ID: {}", id);
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @PutMapping("/books/{id}")
    @PreAuthorize("hasAuthority('LIBRARIAN')")
    public ResponseEntity<BookResponse> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest bookRequest) {
        log.info("Updating book with ID: {}", id);
        return ResponseEntity.ok(bookService.updateBook(id, bookRequest));
    }

    @DeleteMapping("/books/{id}")
    @PreAuthorize("hasAuthority('LIBRARIAN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        log.info("Deleting book with ID: {}", id);
        bookService.deleteBookById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/books/search/title")
    @PreAuthorize("hasAuthority('LIBRARIAN')")
    public ResponseEntity<Page<BookResponse>> searchBooksByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        log.info("Searching books by title: '{}' (page: {}, size: {})", title, page, size);
        return ResponseEntity.ok(bookService.searchBooksByTitle(title, pageable));
    }

    @GetMapping("/books/search/author")
    @PreAuthorize("hasAuthority('LIBRARIAN')")
    public ResponseEntity<Page<BookResponse>> searchBooksByAuthor(
            @RequestParam String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        log.info("Searching books by author: '{}' (page: {}, size: {})", author, page, size);
        return ResponseEntity.ok(bookService.searchBooksByAuthor(author, pageable));
    }

    @GetMapping("/books/available")
    @PreAuthorize("hasAuthority('LIBRARIAN')")
    public ResponseEntity<List<BookResponse>> getAvailableBooks() {
        log.info("Fetching available books");
        return ResponseEntity.ok(bookService.getAvailableBooks());
    }

    //========== Users management ==========

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('LIBRARIAN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Fetching all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('LIBRARIAN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("User ID must be a positive number");
        }
        log.info("Fetching user by ID: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('LIBRARIAN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        if (id == null || id <= 0) {
            throw new BadRequestException("User ID must be a positive number");
        }
        log.info("Updating user with ID: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasAuthority('LIBRARIAN')")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        if (id == null || id <= 0) {
            throw new BadRequestException("User ID must be a positive number");
        }
        log.info("Updating role for user with ID: {}", id);
        return ResponseEntity.ok(userService.updateUserRole(id, request));
    }

    @GetMapping("/users/role/{role}")
    @PreAuthorize("hasAuthority('LIBRARIAN')")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable String role) {
        try {
            Role userRole = Role.valueOf(role.toUpperCase());
            log.info("Fetching users by role: {}", userRole);
            return ResponseEntity.ok(userService.getUsersByRole(userRole));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + role + ". Valid roles are: USER, LIBRARIAN");
        }
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('LIBRARIAN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("User ID must be a positive number");
        }
        log.info("Deleting user with ID: {}", id);
        userService.deleteUserById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
