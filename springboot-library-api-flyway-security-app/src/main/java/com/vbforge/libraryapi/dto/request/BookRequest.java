package com.vbforge.libraryapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookRequest {

    @NotBlank(message = "Title must not be blank")
    @Size(max = 255, message = "Title must be ≤255 characters")
    private String title;

    @NotBlank(message = "Author must not be blank")
    @Size(max = 100, message = "Author must be ≤100 characters")
    private String author;

    @NotBlank(message = "ISBN must not be blank")
    @Pattern(regexp = "^\\d{13}$", message = "ISBN must be 13 digits")
    private String isbn;

    @Min(value = 1, message = "Total copies must be ≥1")
    private int totalCopies;

}
