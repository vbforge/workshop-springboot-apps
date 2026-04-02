package com.vbforge.wookie.service;

import com.vbforge.wookie.dto.request.BookCreateRequest;
import com.vbforge.wookie.dto.request.BookUpdateRequest;
import com.vbforge.wookie.dto.response.BookResponse;
import com.vbforge.wookie.entity.Book;
import com.vbforge.wookie.exception.PermissionDeniedException;
import com.vbforge.wookie.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Book management operations.
 */
public interface BookService {

    BookResponse createBook(BookCreateRequest request, String authorPseudonym);

    Optional<BookResponse> findBookById(Long bookId);

    Optional<BookResponse> findBookByTitle(String title);

    Page<BookResponse> searchBooks(String authorPseudonym, String title, 
                                   BigDecimal minPrice, BigDecimal maxPrice, 
                                   Pageable pageable);

    List<BookResponse> findAllBooks();

    List<BookResponse> findMyBooks(String authorPseudonym);

    BookResponse updateBook(Long bookId, BookUpdateRequest request, String currentUserPseudonym);

    void deleteBook(String title, String currentUserPseudonym);

    boolean canModifyBook(Book book, String currentUserPseudonym);


    /**
     * Toggle publish status of a book (unpublish/republish).
     * @param bookId Book identifier
     * @param publish true to publish, false to unpublish
     * @param currentUserPseudonym Current user's pseudonym
     * @return Updated book response
     * @throws PermissionDeniedException if user is not owner or admin, or if RESTRICTED_USER tries to publish
     * @throws ResourceNotFoundException if book not found
     */
    BookResponse togglePublishStatus(Long bookId, boolean publish, String currentUserPseudonym);

    /**
     * Hard delete a book from database (SUPER_ADMIN only).
     * @param bookId Book identifier
     * @param currentUserPseudonym Current user's pseudonym (must be admin)
     * @throws PermissionDeniedException if user is not SUPER_ADMIN
     * @throws ResourceNotFoundException if book not found
     */
    void hardDeleteBook(Long bookId, String currentUserPseudonym);

    /**
     * Get all unpublished books (for admin cleanup).
     * @return List of unpublished books
     */
    List<BookResponse> findAllUnpublishedBooks();

}