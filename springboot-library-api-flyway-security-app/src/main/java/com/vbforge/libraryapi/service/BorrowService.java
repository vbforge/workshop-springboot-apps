package com.vbforge.libraryapi.service;

import com.vbforge.libraryapi.dto.response.BorrowResponse;
import com.vbforge.libraryapi.entity.BorrowRecord;
import com.vbforge.libraryapi.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BorrowService {

    BorrowResponse borrowBook(Long bookId); // Borrow a book (current user)
    BorrowResponse returnBook(Long bookId); // Return a book (current user)
    List<BorrowResponse> getUserBorrowRecords(); // Get borrow records for current user
    List<BorrowResponse> getAllBorrowRecords(); // For LIBRARIAN: Get all records
    void validateBorrowLimit(User user); // Check if user can borrow more books
    BigDecimal calculateLateFee(BorrowRecord record); // Calculate late fee
    boolean isBookBorrowedByUser(Long bookId, User user); // Check if user borrowed a book
    Optional<BorrowRecord> findActiveBorrowByBookAndUser(Long bookId, User user); // Find active borrow

}
