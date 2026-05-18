package com.vbforge.bookapi.service;

import com.vbforge.bookapi.dto.AuthorDTO;
import com.vbforge.bookapi.dto.BookDTO;

import java.util.List;

/**
 * Service interface for Author business logic
 */
public interface AuthorService {

    /**
     * Create a new author
     */
    AuthorDTO createAuthor(AuthorDTO authorDTO);

    /**
     * Get author by ID
     */
    AuthorDTO getAuthorById(Long id);

    /**
     * Get all authors
     */
    List<AuthorDTO> getAllAuthors();

    /**
     * Update author
     */
    AuthorDTO updateAuthor(Long id, AuthorDTO authorDTO);

    /**
     * Delete author
     */
    void deleteAuthor(Long id);

    /**
     * Search authors by name
     */
    List<AuthorDTO> searchAuthorsByName(String name);

    /**
     * Find authors by nationality
     */
    List<AuthorDTO> getAuthorsByNationality(String nationality);

    /**
     * Get books by author
     */
    List<BookDTO> getBooksByAuthor(Long authorId);

}
