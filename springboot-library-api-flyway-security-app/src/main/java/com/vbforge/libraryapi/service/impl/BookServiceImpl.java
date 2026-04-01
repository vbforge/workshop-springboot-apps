package com.vbforge.libraryapi.service.impl;

import com.vbforge.libraryapi.dto.request.BookRequest;
import com.vbforge.libraryapi.dto.response.BookResponse;
import com.vbforge.libraryapi.entity.Book;
import com.vbforge.libraryapi.exception.custom.BadRequestException;
import com.vbforge.libraryapi.exception.custom.BookNotFoundException;
import com.vbforge.libraryapi.exception.custom.IsbnValidationException;
import com.vbforge.libraryapi.repository.BookRepository;
import com.vbforge.libraryapi.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    @Transactional
    public BookResponse addBook(BookRequest bookRequest) {

        String sanitizedTitle = sanitizeInput(bookRequest.getTitle());
        String sanitizedAuthor = sanitizeInput(bookRequest.getAuthor());
        String sanitizedIsbn = sanitizeInput(bookRequest.getIsbn());

        validateIsbn(sanitizedIsbn);
        if (bookRepository.existsByIsbn(sanitizedIsbn)) {
            throw new IsbnValidationException(sanitizedIsbn);
        }

        Book book = Book.builder()
                .title(sanitizedTitle)
                .author(sanitizedAuthor)
                .isbn(sanitizedIsbn)
                .totalCopies(bookRequest.getTotalCopies())
                .availableCopies(bookRequest.getTotalCopies())
                .build();

        log.info("Adding book: {}", sanitizedTitle);
        return BookResponse.from(bookRepository.save(book));
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("ID must be a positive number");
        }
        return BookResponse.from(
                bookRepository.findById(id)
                        .orElseThrow(() -> new BookNotFoundException(id))
        );
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long id, BookRequest bookRequest) {
        if (id == null || id <= 0 || bookRequest == null) {
            throw new BadRequestException("Invalid ID or Request");
        }

        String sanitizedTitle = sanitizeInput(bookRequest.getTitle());
        String sanitizedAuthor = sanitizeInput(bookRequest.getAuthor());
        String sanitizedIsbn = sanitizeInput(bookRequest.getIsbn());

        validateIsbn(bookRequest.getIsbn());

        Book existBook = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));

        existBook.setTitle(sanitizedTitle);
        existBook.setAuthor(sanitizedAuthor);
        existBook.setIsbn(sanitizedIsbn);
        existBook.setTotalCopies(bookRequest.getTotalCopies());
        existBook.setAvailableCopies(bookRequest.getTotalCopies());

        log.info("Updating book with ID: {}", id);

        return BookResponse.from(bookRepository.save(existBook));
    }

    @Override
    @Transactional
    public void deleteBookById(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("ID must be a positive number");
        }
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        log.debug("Deleting book with ID: {}", book.getId());
        bookRepository.deleteById(id);
        log.info("Deleted book with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BookResponse> findBookByIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new IsbnValidationException(isbn);
        }
        validateIsbn(isbn);
        log.info("Finding book by ISBN: {}", isbn);
        return bookRepository.findByIsbn(isbn).map(BookResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> findBooksByTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BadRequestException("Title must not be blank");
        }
        String sanitizeTitle = sanitizeInput(title);
        return bookRepository.findByTitleContainingIgnoreCase(sanitizeTitle).stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponse> findBooksByAuthor(String author) {
        if (author == null || author.isBlank()) {
            throw new BadRequestException("Author must not be blank");
        }
        String sanitizeAuthor = sanitizeInput(author);
        return bookRepository.findByAuthorContainingIgnoreCase(sanitizeAuthor).stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> findAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(BookResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooksByTitle(String title, Pageable pageable) {
        if (title == null || title.isBlank()) {
            throw new BadRequestException("Title must not be blank");
        }
        String sanitizeTitle = sanitizeInput(title);

        return bookRepository.findByTitleContainingIgnoreCase(sanitizeTitle, pageable)
                .map(BookResponse::from);
    }

    @Override
    public Page<BookResponse> searchBooksByAuthor(String author, Pageable pageable) {
        if (author == null || author.isBlank()) {
            throw new BadRequestException("Author must not be blank");
        }
        String sanitizeAuthor = sanitizeInput(author);

        return bookRepository.findByAuthorContainingIgnoreCase(sanitizeAuthor, pageable)
                .map(BookResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBookExists(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return false;
        }
        validateIsbn(isbn);
        return bookRepository.existsByIsbn(isbn);
    }

    @Override
    public boolean isBookAvailable(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new IsbnValidationException(isbn);
        }
        validateIsbn(isbn);
        return bookRepository.findByIsbn(isbn)
                .map(book -> book.getAvailableCopies() > 0)
                .orElse(false);
    }

    @Override
    @Transactional
    public List<BookResponse> getAvailableBooks() {
        return bookRepository.findByAvailableCopiesGreaterThan(0)
                .stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void decreaseAvailableCopies(String isbn, int copies) {
        if (isbn == null || isbn.isBlank()) {
            throw new IsbnValidationException(isbn);
        }
        validateIsbn(isbn);
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));
        if(book.getAvailableCopies() < copies) {
            throw new BadRequestException("Not enough copies available for ISBN: " + isbn);
        }
        book.setAvailableCopies(book.getAvailableCopies() - copies);
        bookRepository.save(book);
        log.info("Decreased available copies for ISBN: {}", isbn);
    }

    @Override
    @Transactional
    public void increaseAvailableCopies(String isbn, int copies) {
        if (isbn == null || isbn.isBlank()) {
            throw new IsbnValidationException(isbn);
        }
        validateIsbn(isbn);
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));
        book.setAvailableCopies(book.getAvailableCopies() + copies);
        bookRepository.save(book);
        log.info("Increased available copies for ISBN: {}", isbn);
    }

    @Override
    public void validateIsbn(String isbn) {
        if (isbn == null || !isbn.matches("\\d{13}")) {
            throw new IsbnValidationException(isbn);
        }
    }

    @Override
    public void validateBookAvailability(String isbn) {
        if(!isBookAvailable(isbn)) {
            throw new BadRequestException("Book with ISBN: " + isbn + " is not available");
        }
    }

    @Override
    public Book getBookEntityById(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Book ID must be a positive number");
        }
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    // helper method
    private String sanitizeInput(String input) {
        return Encode.forHtml(input.trim());
    }

}
