package com.vbforge.booksapi.repository;

import com.vbforge.booksapi.entity.Book;
import com.vbforge.booksapi.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Check if a book exists with the same title and author (case-insensitive)
    boolean existsByTitleIgnoreCaseAndAuthorIgnoreCase(String title, String author);

    // Find books by genre
    List<Book> findByGenre(Genre genre);

    // Case-insensitive search - only apply filter when parameter has value
    @Query("SELECT b FROM Book b WHERE " +
            "(:titleKeyword IS NULL OR :titleKeyword = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :titleKeyword, '%'))) AND " +
            "(:authorKeyword IS NULL OR :authorKeyword = '' OR LOWER(b.author) LIKE LOWER(CONCAT('%', :authorKeyword, '%')))")
    List<Book> searchBooksCaseInsensitive(@Param("titleKeyword") String titleKeyword,
                                          @Param("authorKeyword") String authorKeyword);

    // For single keyword search (title OR author)
    @Query("SELECT b FROM Book b WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Book> searchByKeyword(@Param("keyword") String keyword);

}
