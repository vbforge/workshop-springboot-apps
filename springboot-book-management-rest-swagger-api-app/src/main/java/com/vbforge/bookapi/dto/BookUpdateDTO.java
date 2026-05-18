package com.vbforge.bookapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * DTO for updating an existing book
 * All fields are optional (only provided fields will be updated)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookUpdateDTO {

    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publicationDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have maximum 10 integer digits and 2 decimal places")
    private BigDecimal price;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;

    @Min(value = 1, message = "Page count must be at least 1")
    private Integer pageCount;

    // IDs for relationships (optional updates)
    private Long categoryId;
    private Long publisherId;
    private Set<Long> authorIds;

}
