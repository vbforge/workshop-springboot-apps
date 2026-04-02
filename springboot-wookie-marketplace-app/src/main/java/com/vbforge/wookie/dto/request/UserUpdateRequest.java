package com.vbforge.wookie.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating existing user information.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Size(min = 3, max = 100, message = "Author pseudonym must be between 3 and 100 characters")
    private String authorPseudonym;

    @Size(min = 6, max = 255, message = "Password must be at least 6 characters")
    private String authorPassword;
}