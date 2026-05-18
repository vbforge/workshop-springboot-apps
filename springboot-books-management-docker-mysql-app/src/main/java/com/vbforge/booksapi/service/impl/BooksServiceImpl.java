package com.vbforge.booksapi.service.impl;

import com.vbforge.booksapi.dto.request.BookRequestDTO;
import com.vbforge.booksapi.dto.response.BookResponseDTO;
import com.vbforge.booksapi.entity.Book;
import com.vbforge.booksapi.entity.Genre;
import com.vbforge.booksapi.exception.BookAlreadyExistException;
import com.vbforge.booksapi.exception.BookNotFoundException;
import com.vbforge.booksapi.mapper.BookMapper;
import com.vbforge.booksapi.repository.BookRepository;
import com.vbforge.booksapi.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BooksServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public List<BookResponseDTO> getAllBooks() {
        log.info("Fetching all books");
        List<Book> books = bookRepository.findAll();
        log.debug("Found {} books in database", books.size());
        return bookMapper.toDTOList(books);
    }

    @Override
    public BookResponseDTO getBookById(Long id) {
        log.info("Fetching book by id: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Book not found with id: {}", id);
                    return new BookNotFoundException(id);
                });
        log.debug("Successfully retrieved book: {} by {}", book.getTitle(), book.getAuthor());
        return bookMapper.toDTO(book);
    }

    @Override
    @Transactional
    public BookResponseDTO insertBook(BookRequestDTO bookRequestDTO) {
        log.info("Attempting to insert new book: '{}' by '{}'", 
                 bookRequestDTO.getTitle(), bookRequestDTO.getAuthor());
        
        // Check if book already exists by title and author
        boolean exists = bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase(
                bookRequestDTO.getTitle(), 
                bookRequestDTO.getAuthor()
        );
        
        if (exists) {
            log.warn("Book already exists with title: '{}' and author: '{}'", 
                     bookRequestDTO.getTitle(), bookRequestDTO.getAuthor());
            throw new BookAlreadyExistException(
                String.format("Book with title '%s' and author '%s' already exists", 
                              bookRequestDTO.getTitle(), bookRequestDTO.getAuthor())
            );
        }
        
        Book book = bookMapper.toEntity(bookRequestDTO);
        Book savedBook = bookRepository.save(book);
        log.info("Successfully inserted book with id: {}, title: '{}'", 
                 savedBook.getId(), savedBook.getTitle());
        
        return bookMapper.toDTO(savedBook);
    }

    @Override
    @Transactional
    public void updateBookById(Long id, BookRequestDTO bookRequestDTO) {
        log.info("Attempting to update book with id: {}", id);
        
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cannot update - book not found with id: {}", id);
                    return new BookNotFoundException(id);
                });
        
        log.debug("Current book data - Title: '{}', Author: '{}'", 
                  existingBook.getTitle(), existingBook.getAuthor());
        
        // Check if the new title+author combination conflicts with another book
        boolean existsConflict = bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase(
                bookRequestDTO.getTitle(), 
                bookRequestDTO.getAuthor()
        );
        
        // If conflict exists and it's not the same book being updated
        if (existsConflict && !existingBook.getTitle().equalsIgnoreCase(bookRequestDTO.getTitle()) 
                && !existingBook.getAuthor().equalsIgnoreCase(bookRequestDTO.getAuthor())) {
            log.warn("Update conflict - Book already exists with title: '{}' and author: '{}'", 
                     bookRequestDTO.getTitle(), bookRequestDTO.getAuthor());
            throw new BookAlreadyExistException(
                String.format("Cannot update. Book with title '%s' and author '%s' already exists", 
                              bookRequestDTO.getTitle(), bookRequestDTO.getAuthor())
            );
        }
        
        bookMapper.updateEntity(existingBook, bookRequestDTO);
        Book updatedBook = bookRepository.save(existingBook);
        log.info("Successfully updated book with id: {}. New title: '{}', Author: '{}'", 
                 id, updatedBook.getTitle(), updatedBook.getAuthor());
    }

    @Override
    @Transactional
    public void deleteBookById(Long id) {
        log.info("Attempting to delete book with id: {}", id);
        
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cannot delete - book not found with id: {}", id);
                    return new BookNotFoundException(id);
                });
        
        log.debug("Found book to delete: '{}' by '{}'", book.getTitle(), book.getAuthor());
        bookRepository.deleteById(id);
        log.info("Successfully deleted book with id: {}, title: '{}'", id, book.getTitle());
    }

    @Override
    public boolean existsByTitleAndAuthor(String title, String author) {
        log.info("Checking existence of book with title: '{}' and author: '{}'", title, author);
        boolean exists = bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase(title, author);
        log.debug("Book exists: {}", exists);
        return exists;
    }

    @Override
    public List<BookResponseDTO> getBooksByGenre(Genre genre) {
        log.info("Fetching books by genre: {}", genre);
        List<Book> books = bookRepository.findByGenre(genre);
        log.debug("Found {} books in genre '{}'", books.size(), genre);
        
        if (books.isEmpty()) {
            log.warn("No books found in genre: {}", genre);
        }
        
        return bookMapper.toDTOList(books);
    }

    @Override
    public List<BookResponseDTO> searchBooks(String keyword) {
        log.info("Searching books with keyword: '{}'", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("Empty search keyword provided, returning all books");
            return getAllBooks();
        }

        // Use searchByKeyword for OR logic (title OR author)
        List<Book> books = bookRepository.searchByKeyword(keyword);
        log.debug("Found {} books matching keyword '{}'", books.size(), keyword);
        return bookMapper.toDTOList(books);
    }

    @Override
    public List<BookResponseDTO> searchBooksByTitleOrAuthor(String titleKeyword, String authorKeyword) {
        log.info("Advanced search - Title keyword: '{}', Author keyword: '{}'", titleKeyword, authorKeyword);

        // Handle null or empty keywords
        String titleSearch = (titleKeyword == null || titleKeyword.trim().isEmpty()) ? null : titleKeyword;
        String authorSearch = (authorKeyword == null || authorKeyword.trim().isEmpty()) ? null : authorKeyword;

        // Use searchBooksCaseInsensitive for AND logic (title AND author)
        List<Book> books = bookRepository.searchBooksCaseInsensitive(titleSearch, authorSearch);
        log.debug("Found {} books matching search criteria", books.size());
        return bookMapper.toDTOList(books);
    }

    @Override
    public long getBookCountByGenre(Genre genre) {
        log.info("Counting books by genre: {}", genre);
        List<Book> books = bookRepository.findByGenre(genre);
        long count = books.size();
        log.debug("Found {} books in genre '{}'", count, genre);
        return count;
    }
}