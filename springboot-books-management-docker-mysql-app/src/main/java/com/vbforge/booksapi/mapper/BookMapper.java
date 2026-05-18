package com.vbforge.booksapi.mapper;


import com.vbforge.booksapi.dto.request.BookRequestDTO;
import com.vbforge.booksapi.dto.response.BookResponseDTO;
import com.vbforge.booksapi.entity.Book;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookMapper {
    
    public BookResponseDTO toDTO(Book book) {
        if (book == null) {
            return null;
        }
        
        return BookResponseDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .description(book.getDescription())
                .author(book.getAuthor())
                .genre(book.getGenre())
                .build();
    }
    
    public Book toEntity(BookRequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }
        
        return Book.builder()
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .author(requestDTO.getAuthor())
                .genre(requestDTO.getGenre())
                .build();
    }
    
    public void updateEntity(Book existingBook, BookRequestDTO requestDTO) {
        if (existingBook == null || requestDTO == null) {
            return;
        }
        
        existingBook.setTitle(requestDTO.getTitle());
        existingBook.setDescription(requestDTO.getDescription());
        existingBook.setAuthor(requestDTO.getAuthor());
        existingBook.setGenre(requestDTO.getGenre());
    }
    
    public List<BookResponseDTO> toDTOList(List<Book> books) {
        if (books == null) {
            return List.of();
        }
        
        return books.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}