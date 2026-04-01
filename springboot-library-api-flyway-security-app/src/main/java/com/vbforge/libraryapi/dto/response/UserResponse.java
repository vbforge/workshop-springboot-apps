package com.vbforge.libraryapi.dto.response;


import com.vbforge.libraryapi.entity.Role;
import com.vbforge.libraryapi.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getSignupDate())
                .build();
    }
}
