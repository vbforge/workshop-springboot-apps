package com.vbforge.libraryapi.service;

import com.vbforge.libraryapi.dto.request.LoginRequest;
import com.vbforge.libraryapi.dto.request.SignupRequest;
import com.vbforge.libraryapi.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse signup(SignupRequest signupRequest);
    AuthResponse login(LoginRequest request);


}
