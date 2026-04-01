package com.vbforge.libraryapi.repository;

import com.vbforge.libraryapi.entity.Role;
import com.vbforge.libraryapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByRole(Role role);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findByRole(Role role); // Filter users by role (e.g., LIBRARIAN/USER)

}
