package com.vbforge.libraryapi.dto.response;


import com.vbforge.libraryapi.entity.Role;
import com.vbforge.libraryapi.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LibrarianResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;

    public static LibrarianResponse from(User librarian) {

        return LibrarianResponse.builder()
                .id(librarian.getId())
                .username(librarian.getUsername())
                .email(librarian.getEmail())
                .role(librarian.getRole())
                .createdAt(librarian.getSignupDate())
                .build();
    }

}
