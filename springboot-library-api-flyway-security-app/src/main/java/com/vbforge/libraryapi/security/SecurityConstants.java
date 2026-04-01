package com.vbforge.libraryapi.security;

public final class SecurityConstants {
    public static final String API_HEALTH = "/api/health";
    public static final String API_AUTH = "/api/auth/**";
    public static final String API_LIBRARIAN = "/api/librarian/**";
    public static final String API_USER = "/api/user/**";
    public static final String ROLE_LIBRARIAN = "LIBRARIAN";
    public static final String ROLE_USER = "USER";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
}