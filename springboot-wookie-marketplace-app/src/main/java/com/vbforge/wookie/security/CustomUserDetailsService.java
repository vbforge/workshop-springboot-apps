package com.vbforge.wookie.security;

import com.vbforge.wookie.entity.User;
import com.vbforge.wookie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        
        User user = userRepository.findByAuthorPseudonym(username)
                .orElseThrow(() -> {
                    log.error("User not found with pseudonym: {}", username);
                    return new UsernameNotFoundException("User not found with pseudonym: " + username);
                });
        
        if (!user.isEnabled()) {
            log.warn("User account is disabled: {}", username);
            throw new UsernameNotFoundException("User account is disabled");
        }
        
        log.debug("User loaded successfully: {}", username);
        return user;
    }
}