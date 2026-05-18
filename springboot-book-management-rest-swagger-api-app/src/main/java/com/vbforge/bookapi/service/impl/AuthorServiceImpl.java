package com.vbforge.bookapi.service.impl;

import com.vbforge.bookapi.dto.AuthorDTO;
import com.vbforge.bookapi.dto.BookDTO;
import com.vbforge.bookapi.entity.Author;
import com.vbforge.bookapi.entity.Book;
import com.vbforge.bookapi.exception.DuplicateResourceException;
import com.vbforge.bookapi.exception.ResourceNotFoundException;
import com.vbforge.bookapi.mapper.BookMapper;
import com.vbforge.bookapi.repository.AuthorRepository;
import com.vbforge.bookapi.repository.BookRepository;
import com.vbforge.bookapi.service.AuthorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of AuthorService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthorServiceImpl implements AuthorService{

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    @Transactional
    public AuthorDTO createAuthor(AuthorDTO authorDTO) {
        log.info("Creating new author: {}", authorDTO.getName());

        // Validate name uniqueness
        if (authorRepository.existsByName(authorDTO.getName())) {
            throw new DuplicateResourceException("Author", "name", authorDTO.getName());
        }

        Author author = bookMapper.toEntity(authorDTO);
        Author savedAuthor = authorRepository.save(author);

        log.info("Author created successfully with ID: {}", savedAuthor.getId());
        return bookMapper.toDTO(savedAuthor);
    }

    @Override
    public AuthorDTO getAuthorById(Long id) {
        log.debug("Fetching author with ID: {}", id);

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with ID: " + id));

        return bookMapper.toDTO(author);
    }

    @Override
    public List<AuthorDTO> getAllAuthors() {
        log.debug("Fetching all authors");

        List<Author> authors = authorRepository.findAll();
        return bookMapper.authorsToDTOList(authors);
    }

    @Override
    @Transactional
    public AuthorDTO updateAuthor(Long id, AuthorDTO authorDTO) {
        log.info("Updating author with ID: {}", id);

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with ID: " + id));

        // Check name uniqueness if name is being changed
        if (!author.getName().equals(authorDTO.getName()) && authorRepository.existsByName(authorDTO.getName())) {
            throw new DuplicateResourceException("Author", "name", authorDTO.getName());
        }

        // Update fields
        author.setName(authorDTO.getName());
        author.setBiography(authorDTO.getBiography());
        author.setDateOfBirth(authorDTO.getDateOfBirth());
        author.setNationality(authorDTO.getNationality());

        Author updatedAuthor = authorRepository.save(author);
        log.info("Author updated successfully with ID: {}", updatedAuthor.getId());

        return bookMapper.toDTO(updatedAuthor);
    }

    @Override
    @Transactional
    public void deleteAuthor(Long id) {
        log.info("Deleting author with ID: {}", id);

        if (!authorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Author not found with ID: " + id);
        }

        authorRepository.deleteById(id);
        log.info("Author deleted successfully with ID: {}", id);
    }

    @Override
    public List<AuthorDTO> searchAuthorsByName(String name) {
        log.debug("Searching authors by name: {}", name);

        List<Author> authors = authorRepository.findByNameContainingIgnoreCase(name);
        return bookMapper.authorsToDTOList(authors);
    }

    @Override
    public List<AuthorDTO> getAuthorsByNationality(String nationality) {
        log.debug("Fetching authors by nationality: {}", nationality);

        List<Author> authors = authorRepository.findByNationalityIgnoreCase(nationality);
        return bookMapper.authorsToDTOList(authors);
    }

    @Override
    public List<BookDTO> getBooksByAuthor(Long authorId) {
        log.debug("Fetching books by author ID: {}", authorId);

        if (!authorRepository.existsById(authorId)) {
            throw new ResourceNotFoundException("Author not found with ID: " + authorId);
        }

        List<Book> books = bookRepository.findByAuthorId(authorId);
        return bookMapper.toDTOList(books);
    }

}
