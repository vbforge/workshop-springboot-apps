package com.vbforge.bookapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Publisher entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublisherDTO {

    private Long id;

    @NotBlank(message = "Publisher name is required")
    @Size(min = 2, max = 255, message = "Publisher name must be between 2 and 255 characters")
    private String name;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 255, message = "Website must not exceed 255 characters")
    private String website;

    @Email(message = "Contact email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String contactEmail;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Optional: Book count
    private Integer bookCount;

}
