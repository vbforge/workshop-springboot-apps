package com.vbforge.libraryapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, updatable = false, name = "signup_date")
    private LocalDateTime signupDate;

    @Column(nullable = false, name = "login_date")
    private LocalDateTime loginDate;

    @PrePersist
    public void prePersist() {
        this.loginDate = LocalDateTime.now();
        this.signupDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.loginDate = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority>  getAuthorities() {
        return List.of(this.role.toAuthority());
    }

    @Override
    public boolean isAccountNonExpired()  { return true; }

    @Override
    public boolean isAccountNonLocked()   { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled()            { return true; }

}