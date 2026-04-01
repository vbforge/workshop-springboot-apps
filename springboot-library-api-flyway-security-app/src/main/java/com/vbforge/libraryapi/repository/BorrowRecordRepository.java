package com.vbforge.libraryapi.repository;

import com.vbforge.libraryapi.entity.Book;
import com.vbforge.libraryapi.entity.BorrowRecord;
import com.vbforge.libraryapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByUserAndReturnDateIsNull(User user); // Active borrows

    List<BorrowRecord> findByUser(User user); // All borrows for a user

    Optional<BorrowRecord> findByUserAndBookAndReturnDateIsNull(User user, Book book); // Check if user borrowed a book

    long countByUserAndReturnDateIsNull(User user); // Count active borrows (for max limit)


}
