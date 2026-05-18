package com.vbforge.bookapi.repository;

import com.vbforge.bookapi.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Publisher entity
 */
@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long>{

    /**
     * Find publisher by name (exact match)
     */
    Optional<Publisher> findByName(String name);

    /**
     * Find publishers by name containing (case-insensitive)
     */
    List<Publisher> findByNameContainingIgnoreCase(String name);

    /**
     * Find publisher by contact email
     */
    Optional<Publisher> findByContactEmail(String contactEmail);

    /**
     * Check if publisher exists by name
     */
    boolean existsByName(String name);

    /**
     * Check if publisher exists by contact email
     */
    boolean existsByContactEmail(String contactEmail);

    /**
     * Find publisher with all its books
     */
    @Query("SELECT p FROM Publisher p LEFT JOIN FETCH p.books WHERE p.id = :id")
    Optional<Publisher> findByIdWithBooks(@Param("id") Long id);

    /**
     * Find all publishers with their books
     */
    @Query("SELECT DISTINCT p FROM Publisher p LEFT JOIN FETCH p.books")
    List<Publisher> findAllWithBooks();

    /**
     * Find publishers that have published books
     */
    @Query("SELECT DISTINCT p FROM Publisher p WHERE SIZE(p.books) > 0")
    List<Publisher> findPublishersWithBooks();

    /**
     * Count books by publisher
     */
    @Query("SELECT COUNT(b) FROM Book b WHERE b.publisher.id = :publisherId")
    Long countBooksByPublisher(@Param("publisherId") Long publisherId);


}
