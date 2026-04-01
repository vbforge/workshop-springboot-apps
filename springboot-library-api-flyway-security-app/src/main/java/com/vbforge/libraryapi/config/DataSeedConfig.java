package com.vbforge.libraryapi.config;

import com.vbforge.libraryapi.entity.Role;
import com.vbforge.libraryapi.entity.User;
import com.vbforge.libraryapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeedConfig implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LibrarianProperties librarianProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(Role.LIBRARIAN)) {
            log.info("LIBRARIAN already exists — skipping bootstrap.");
            return;
        }

        User librarian = User.builder()
                .username(librarianProperties.getUsername())
                .email(librarianProperties.getEmail())
                .password(passwordEncoder.encode(librarianProperties.getPassword()))
                .role(Role.LIBRARIAN)
                .build();

        userRepository.save(librarian);
        log.info("Bootstrap: LIBRARIAN account '{}' created.", librarianProperties.getUsername());
    }
}