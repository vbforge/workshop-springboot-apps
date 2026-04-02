package com.vbforge.wookie.service.impl;

import com.vbforge.wookie.dto.request.BookCreateRequest;
import com.vbforge.wookie.dto.request.BookUpdateRequest;
import com.vbforge.wookie.dto.response.BookResponse;
import com.vbforge.wookie.dto.response.UserResponse;
import com.vbforge.wookie.entity.Book;
import com.vbforge.wookie.entity.Roles;
import com.vbforge.wookie.entity.User;
import com.vbforge.wookie.exception.DuplicateResourceException;
import com.vbforge.wookie.exception.PermissionDeniedException;
import com.vbforge.wookie.exception.ResourceNotFoundException;
import com.vbforge.wookie.repository.BookRepository;
import com.vbforge.wookie.repository.UserRepository;
import com.vbforge.wookie.service.BookService;
import com.vbforge.wookie.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookResponse createBook(BookCreateRequest request, String authorPseudonym) {
        log.debug("Creating new book with title: {} by author: {}", request.getTitle(), authorPseudonym);

        // Get current user
        User author = userRepository.findByAuthorPseudonym(authorPseudonym)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authorPseudonym));

        // Check if user is restricted (Darth Vader cannot publish)
        if (author.getRole() == Roles.RESTRICTED_USER) {
            log.warn("Restricted user {} attempted to create a book", authorPseudonym);
            throw new PermissionDeniedException("Restricted users are not allowed to publish books");
        }

        // Check for duplicate title
        if (bookRepository.findByTitle(request.getTitle()).isPresent()) {
            throw new DuplicateResourceException("Book with title '" + request.getTitle() + "' already exists");
        }

        // Create and save book
        Book book = Book.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .author(author)
                .coverImage(request.getCoverImage())
                .price(request.getPrice())
                .isPublished(true)
                .build();

        Book savedBook = bookRepository.save(book);
        log.info("Book created successfully with ID: {} by author: {}", savedBook.getBookId(), authorPseudonym);

        return mapToBookResponse(savedBook);
    }

    @Override
    public Optional<BookResponse> findBookById(Long bookId) {
        log.debug("Fetching book by ID: {}", bookId);
        return bookRepository.findById(bookId)
                .filter(Book::getIsPublished)
                .map(this::mapToBookResponse);
    }

    @Override
    public Optional<BookResponse> findBookByTitle(String title) {
        log.debug("Fetching book by title: {}", title);
        return bookRepository.findByTitle(title)
                .filter(Book::getIsPublished)
                .map(this::mapToBookResponse);
    }

    @Override
    public Page<BookResponse> searchBooks(String authorPseudonym, String title,
                                          BigDecimal minPrice, BigDecimal maxPrice,
                                          Pageable pageable) {
        log.debug("Searching books with filters - author: {}, title: {}, minPrice: {}, maxPrice: {}",
                authorPseudonym, title, minPrice, maxPrice);

        Page<Book> books = bookRepository.searchBooks(authorPseudonym, title, minPrice, maxPrice, pageable);
        return books.map(this::mapToBookResponse);
    }

    @Override
    public List<BookResponse> findAllBooks() {
        log.debug("Fetching all books (admin only)");
        // This method should be called only by admins
        return bookRepository.findAll().stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponse> findMyBooks(String authorPseudonym) {
        log.debug("Fetching books for author: {}", authorPseudonym);

        return bookRepository.findByAuthor_UserIdAndIsPublishedTrue(
                userRepository.findByAuthorPseudonym(authorPseudonym)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                        .getUserId()
        ).stream()
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long bookId, BookUpdateRequest request, String currentUserPseudonym) {
        log.debug("Updating book with ID: {} by user: {}", bookId, currentUserPseudonym);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        // Check permission
        if (!canModifyBook(book, currentUserPseudonym)) {
            throw new PermissionDeniedException("You don't have permission to modify this book");
        }

        // Update fields if provided
        if (request.getTitle() != null && !request.getTitle().equals(book.getTitle())) {
            if (bookRepository.findByTitle(request.getTitle()).isPresent()) {
                throw new DuplicateResourceException("Book with title '" + request.getTitle() + "' already exists");
            }
            book.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            book.setDescription(request.getDescription());
        }

        if (request.getCoverImage() != null) {
            book.setCoverImage(request.getCoverImage());
        }

        if (request.getPrice() != null) {
            book.setPrice(request.getPrice());
        }

        Book updatedBook = bookRepository.save(book);
        log.info("Book updated successfully with ID: {}", bookId);

        return mapToBookResponse(updatedBook);
    }

    @Override
    @Transactional
    public void deleteBook(String title, String currentUserPseudonym) {
        log.debug("Deleting book with title: {} by user: {}", title, currentUserPseudonym);

        Book book = bookRepository.findByTitle(title)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with title: " + title));

        // Check permission
        if (!canModifyBook(book, currentUserPseudonym)) {
            throw new PermissionDeniedException("You don't have permission to delete this book");
        }

        // Soft delete - set as unpublished instead of hard delete
        book.setIsPublished(false);
        bookRepository.save(book);

        log.info("Book unpublished successfully: {}", title);
    }

    @Override
    public boolean canModifyBook(Book book, String currentUserPseudonym) {
        User currentUser = userRepository.findByAuthorPseudonym(currentUserPseudonym)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // SUPER_ADMIN can modify any book
        if (currentUser.getRole() == Roles.SUPER_ADMIN) {
            return true;
        }

        // User can modify their own books
        return book.getAuthor().getAuthorPseudonym().equals(currentUserPseudonym);
    }

    @Override
    @Transactional
    public BookResponse togglePublishStatus(Long bookId, boolean publish, String currentUserPseudonym) {
        log.debug("Toggling publish status for book ID: {} to {} by user: {}",
                bookId, publish, currentUserPseudonym);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        // Check permission
        if (!canModifyBook(book, currentUserPseudonym)) {
            throw new PermissionDeniedException("You don't have permission to modify this book");
        }

        // Check if RESTRICTED_USER is trying to publish
        if (publish && !book.getIsPublished()) {
            User currentUser = userRepository.findByAuthorPseudonym(currentUserPseudonym)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (currentUser.getRole() == Roles.RESTRICTED_USER) {
                throw new PermissionDeniedException("Restricted users are not allowed to publish books");
            }
        }

        book.setIsPublished(publish);
        Book updatedBook = bookRepository.save(book);

        log.info("Book {} successfully: {}",
                publish ? "published" : "unpublished",
                updatedBook.getBookId());

        return mapToBookResponse(updatedBook);
    }

    @Override
    @Transactional
    public void hardDeleteBook(Long bookId, String currentUserPseudonym) {
        log.warn("Attempting to hard delete book ID: {} by user: {}", bookId, currentUserPseudonym);

        // Verify user is SUPER_ADMIN
        User currentUser = userRepository.findByAuthorPseudonym(currentUserPseudonym)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getRole() != Roles.SUPER_ADMIN) {
            log.warn("Non-admin user {} attempted to hard delete book", currentUserPseudonym);
            throw new PermissionDeniedException("Only SUPER_ADMIN can permanently delete books");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        bookRepository.delete(book);
        log.info("Book permanently deleted with ID: {} by admin: {}", bookId, currentUserPseudonym);
    }

    @Override
    public List<BookResponse> findAllUnpublishedBooks() {
        log.debug("Fetching all unpublished books");

        return bookRepository.findAll().stream()
                .filter(book -> !book.getIsPublished())
                .map(this::mapToBookResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map Book entity to BookResponse DTO
     */
    private BookResponse mapToBookResponse(Book book) {
        return BookResponse.builder()
                .bookId(book.getBookId())
                .title(book.getTitle())
                .description(book.getDescription())
                .author(UserResponse.builder()
                        .userId(book.getAuthor().getUserId())
                        .authorPseudonym(book.getAuthor().getAuthorPseudonym())
                        .role(book.getAuthor().getRole().name())
                        .isActive(book.getAuthor().getIsActive())
                        .createdAt(book.getAuthor().getCreatedAt())
                        .build())
                .coverImage(book.getCoverImage())
                .price(book.getPrice())
                .isPublished(book.getIsPublished())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}