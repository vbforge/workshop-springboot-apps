package com.vbforge.booksapi.dto.response;

import com.vbforge.booksapi.entity.Genre;  // Fixed import
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String author;
    private Genre genre;
}