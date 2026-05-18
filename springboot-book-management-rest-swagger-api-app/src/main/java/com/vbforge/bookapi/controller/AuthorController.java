package com.vbforge.bookapi.controller;

import com.vbforge.bookapi.dto.AuthorDTO;
import com.vbforge.bookapi.dto.BookDTO;
import com.vbforge.bookapi.dto.response.ApiResponse;
import com.vbforge.bookapi.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Author management
 * Base path: /api/authors
 */
@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authors", description = "Author management APIs")
public class AuthorController {

    private final AuthorService authorService;

    @PostMapping
    @Operation(summary = "Create a new author", description = "Creates a new author in the system")
    public ResponseEntity<ApiResponse<AuthorDTO>> createAuthor(
            @Valid @RequestBody AuthorDTO authorDTO) {
        log.info("REST request to create author: {}", authorDTO.getName());

        AuthorDTO createdAuthor = authorService.createAuthor(authorDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Author created successfully", createdAuthor));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get author by ID", description = "Returns a single author by its ID")
    public ResponseEntity<ApiResponse<AuthorDTO>> getAuthorById(
            @Parameter(description = "Author ID") @PathVariable Long id) {
        log.info("REST request to get author by ID: {}", id);

        AuthorDTO author = authorService.getAuthorById(id);

        return ResponseEntity.ok(ApiResponse.success(author));
    }

    @GetMapping
    @Operation(summary = "Get all authors", description = "Returns all authors")
    public ResponseEntity<ApiResponse<List<AuthorDTO>>> getAllAuthors() {
        log.info("REST request to get all authors");

        List<AuthorDTO> authors = authorService.getAllAuthors();

        return ResponseEntity.ok(ApiResponse.success(authors));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update author", description = "Updates an existing author")
    public ResponseEntity<ApiResponse<AuthorDTO>> updateAuthor(
            @Parameter(description = "Author ID") @PathVariable Long id,
            @Valid @RequestBody AuthorDTO authorDTO) {
        log.info("REST request to update author with ID: {}", id);

        AuthorDTO updatedAuthor = authorService.updateAuthor(id, authorDTO);

        return ResponseEntity.ok(ApiResponse.success("Author updated successfully", updatedAuthor));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete author", description = "Deletes an author from the system")
    public ResponseEntity<ApiResponse<Void>> deleteAuthor(
            @Parameter(description = "Author ID") @PathVariable Long id) {
        log.info("REST request to delete author with ID: {}", id);

        authorService.deleteAuthor(id);

        return ResponseEntity.ok(ApiResponse.success("Author deleted successfully", null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search authors", description = "Search authors by name")
    public ResponseEntity<ApiResponse<List<AuthorDTO>>> searchAuthors(
            @Parameter(description = "Author name") @RequestParam String name) {
        log.info("REST request to search authors with name: {}", name);

        List<AuthorDTO> authors = authorService.searchAuthorsByName(name);

        return ResponseEntity.ok(ApiResponse.success(authors));
    }

    @GetMapping("/nationality/{nationality}")
    @Operation(summary = "Get authors by nationality", description = "Returns all authors of a specific nationality")
    public ResponseEntity<ApiResponse<List<AuthorDTO>>> getAuthorsByNationality(
            @Parameter(description = "Nationality") @PathVariable String nationality) {
        log.info("REST request to get authors by nationality: {}", nationality);

        List<AuthorDTO> authors = authorService.getAuthorsByNationality(nationality);

        return ResponseEntity.ok(ApiResponse.success(authors));
    }

    @GetMapping("/{id}/books")
    @Operation(summary = "Get books by author", description = "Returns all books written by a specific author")
    public ResponseEntity<ApiResponse<List<BookDTO>>> getBooksByAuthor(
            @Parameter(description = "Author ID") @PathVariable Long id) {
        log.info("REST request to get books by author ID: {}", id);

        List<BookDTO> books = authorService.getBooksByAuthor(id);

        return ResponseEntity.ok(ApiResponse.success(books));
    }
}
