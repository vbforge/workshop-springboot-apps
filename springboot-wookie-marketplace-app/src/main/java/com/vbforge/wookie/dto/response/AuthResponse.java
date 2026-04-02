package com.vbforge.wookie.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type;
    private String username;
    private String role;
    private Long expiresIn;

    public AuthResponse(String token, String username, String role, Long expiresIn) {
        this.token = token;
        this.type = "Bearer";
        this.username = username;
        this.role = role;
        this.expiresIn = expiresIn;
    }

}