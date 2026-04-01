package com.vbforge.libraryapi.entity;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Role {

    USER,
    LIBRARIAN;

    public GrantedAuthority toAuthority() {
        return new SimpleGrantedAuthority(this.name());
    }

}
