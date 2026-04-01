package com.vbforge.libraryapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(min = 3, max = 100, message = "Username must be 3–100 characters")
    private String username;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be ≤100 characters")
    private String email;

    @Size(min = 8, message = "Password must be ≥8 characters")
    private String password; // Optional (null if not updating)
}