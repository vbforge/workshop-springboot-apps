package com.vbforge.booksapi.service;

import com.vbforge.booksapi.dto.request.BookRequestDTO;
import com.vbforge.booksapi.dto.response.BookResponseDTO;
import com.vbforge.booksapi.entity.Genre;

import java.util.List;

public interface BookService {

    List<BookResponseDTO> getAllBooks();

    BookResponseDTO getBookById(Long id);

    BookResponseDTO insertBook(BookRequestDTO bookRequestDTO);

    void updateBookById(Long id, BookRequestDTO bookRequestDTO);

    void deleteBookById(Long id);

    boolean existsByTitleAndAuthor(String title, String author);

    List<BookResponseDTO> getBooksByGenre(Genre genre);

    List<BookResponseDTO> searchBooks(String keyword);

    List<BookResponseDTO> searchBooksByTitleOrAuthor(String titleKeyword, String authorKeyword);

    long getBookCountByGenre(Genre genre);

}
