package com.vbforge.bookapi.repository;

import com.vbforge.bookapi.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category entity
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find category by name (exact match)
     */
    Optional<Category> findByName(String name);

    /**
     * Find categories by name containing (case-insensitive)
     */
    List<Category> findByNameContainingIgnoreCase(String name);

    /**
     * Check if category exists by name
     */
    boolean existsByName(String name);

    /**
     * Find category with all its books
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.books WHERE c.id = :id")
    Optional<Category> findByIdWithBooks(@Param("id") Long id);

    /**
     * Find all categories with book count
     */
    @Query("SELECT c, COUNT(b) FROM Category c LEFT JOIN c.books b GROUP BY c")
    List<Object[]> findAllWithBookCount();

    /**
     * Find categories that have books
     */
    @Query("SELECT DISTINCT c FROM Category c WHERE SIZE(c.books) > 0")
    List<Category> findCategoriesWithBooks();

    /**
     * Find categories without books
     */
    @Query("SELECT c FROM Category c WHERE SIZE(c.books) = 0")
    List<Category> findCategoriesWithoutBooks();

}
