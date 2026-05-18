package com.vbforge.bookapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO for Book entity
 * Used for API responses with full details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Book information with full details")
public class BookDTO {

    @Schema(description = "Unique identifier of the book", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ISBN number of the book", example = "978-0-7475-3269-9", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$",
            message = "ISBN must be valid")
    private String isbn;

    @Schema(description = "Title of the book", example = "Harry Potter and the Philosopher's Stone", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;

    @Schema(description = "Detailed description of the book", example = "The first novel in the Harry Potter series")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Schema(description = "Publication date of the book", example = "1997-06-26")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publicationDate;

    @Schema(description = "Price of the book", example = "19.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have maximum 10 integer digits and 2 decimal places")
    private BigDecimal price;

    @Schema(description = "Available stock quantity", example = "150", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Schema(description = "Language of the book", example = "English")
    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;

    @Schema(description = "Number of pages", example = "223")
    @Min(value = 1, message = "Page count must be at least 1")
    private Integer pageCount;

    @Schema(description = "Category of the book")
    private CategoryDTO category;

    @Schema(description = "Publisher of the book")
    private PublisherDTO publisher;

    @Schema(description = "List of authors")
    @Builder.Default
    private Set<AuthorDTO> authors = new HashSet<>();

    @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
