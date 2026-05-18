package com.vbforge.bookapi.repository;

import com.vbforge.bookapi.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Author entity
 * Spring Data JPA will automatically implement this interface
 */
@Repository
public interface AuthorRepository extends JpaRepository<Author, Long>{

    /**
     * Find author by name (exact match)
     * Method name query - Spring Data JPA generates the query
     */
    Optional<Author> findByName(String name);

    /**
     * Find authors by name containing (case-insensitive)
     * Method name query
     */
    List<Author> findByNameContainingIgnoreCase(String name);

    /**
     * Find authors by nationality
     * Method name query
     */
    List<Author> findByNationality(String nationality);

    /**
     * Find authors by nationality (case-insensitive)
     */
    List<Author> findByNationalityIgnoreCase(String nationality);

    /**
     * Check if author exists by name
     */
    boolean existsByName(String name);

    /**
     * Find authors with books (using JPQL query)
     * @Query annotation for custom JPQL queries
     */
    @Query("SELECT DISTINCT a FROM Author a LEFT JOIN FETCH a.books WHERE a.id = :id")
    Optional<Author> findByIdWithBooks(@Param("id") Long id);

    /**
     * Find all authors with their books
     * Custom JPQL query with JOIN FETCH for eager loading
     */
    @Query("SELECT DISTINCT a FROM Author a LEFT JOIN FETCH a.books")
    List<Author> findAllWithBooks();

    /**
     * Count authors by nationality
     */
    @Query("SELECT COUNT(a) FROM Author a WHERE a.nationality = :nationality")
    Long countByNationality(@Param("nationality") String nationality);

}
