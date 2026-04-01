package com.vbforge.libraryapi.service;

import com.vbforge.libraryapi.dto.request.BookRequest;
import com.vbforge.libraryapi.dto.response.BookResponse;
import com.vbforge.libraryapi.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

/**
 * business logic for books
 *  - Implement CRUD operations for books.
 *  - Validate ISBN format and book availability.
 * */
public interface BookService {

    //crud
    BookResponse addBook(BookRequest bookRequest);
    BookResponse getBookById(Long id);
    BookResponse updateBook(Long id, BookRequest bookRequest);
    void deleteBookById(Long id);

    //search & filter
    Optional<BookResponse> findBookByIsbn(String isbn);
    List<BookResponse> findBooksByTitle(String title);
    List<BookResponse> findBooksByAuthor(String author);
    Page<BookResponse> findAllBooks(Pageable pageable);
    Page<BookResponse> searchBooksByTitle(String title, Pageable pageable);
    Page<BookResponse> searchBooksByAuthor(String author, Pageable pageable);

    //business
    boolean isBookExists(String isbn);
    boolean isBookAvailable(String isbn);
    List<BookResponse> getAvailableBooks();
    void decreaseAvailableCopies(String isbn, int copies);
    void increaseAvailableCopies(String isbn, int copies);

    //validation
    void validateIsbn(String isbn);
    void validateBookAvailability(String isbn);

    Book getBookEntityById(Long id);

}












