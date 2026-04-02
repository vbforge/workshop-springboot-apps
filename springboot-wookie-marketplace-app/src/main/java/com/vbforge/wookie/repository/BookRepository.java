package com.vbforge.wookie.repository;

import com.vbforge.wookie.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    Optional<Book> findByTitle(String title);
    
    List<Book> findByAuthor_UserIdAndIsPublishedTrue(Long userId);
    
    @Query("SELECT b FROM Book b WHERE b.author.authorPseudonym = :authorPseudonym AND b.isPublished = true")
    List<Book> findByAuthorPseudonym(@Param("authorPseudonym") String authorPseudonym);
    
    @Query("SELECT b FROM Book b WHERE " +
           "(:authorPseudonym IS NULL OR b.author.authorPseudonym = :authorPseudonym) AND " +
           "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:minPrice IS NULL OR b.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR b.price <= :maxPrice) AND " +
           "b.isPublished = true")
    Page<Book> searchBooks(@Param("authorPseudonym") String authorPseudonym,
                          @Param("title") String title,
                          @Param("minPrice") BigDecimal minPrice,
                          @Param("maxPrice") BigDecimal maxPrice,
                          Pageable pageable);
    
    long countByAuthor_UserId(Long userId);
}