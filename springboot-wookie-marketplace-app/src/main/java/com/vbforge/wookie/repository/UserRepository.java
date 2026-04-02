package com.vbforge.wookie.repository;

import com.vbforge.wookie.entity.Roles;
import com.vbforge.wookie.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByAuthorPseudonym(String authorPseudonym);
    
    Optional<User> findByRole(Roles role);
    
    boolean existsByAuthorPseudonym(String authorPseudonym);
    
    Optional<User> findByAuthorPseudonymAndIsActiveTrue(String authorPseudonym);
}