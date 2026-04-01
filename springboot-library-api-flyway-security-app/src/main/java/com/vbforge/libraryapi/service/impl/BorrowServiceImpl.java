package com.vbforge.libraryapi.service.impl;

import com.vbforge.libraryapi.dto.response.BorrowResponse;
import com.vbforge.libraryapi.entity.Book;
import com.vbforge.libraryapi.entity.BorrowRecord;
import com.vbforge.libraryapi.entity.User;
import com.vbforge.libraryapi.exception.custom.*;
import com.vbforge.libraryapi.repository.BorrowRecordRepository;
import com.vbforge.libraryapi.repository.UserRepository;
import com.vbforge.libraryapi.service.BookService;
import com.vbforge.libraryapi.service.BorrowService;
import com.vbforge.libraryapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookService bookService;
    private final UserRepository userRepository;


    // === Helper Methods ===
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username) // Now works
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private void validateBookAvailability(Long bookId) {
        if (!bookService.isBookAvailable(bookId.toString())) {
            throw new BookUnavailableException(
                bookService.getBookById(bookId).getTitle()
            );
        }
    }

    // === Core Methods ===
    @Override
    @Transactional
    public BorrowResponse borrowBook(Long bookId) {
        User currentUser = getCurrentUser();
        Book book = bookService.getBookEntityById(bookId);  // Fetch book by ID

        // Validate the book's ISBN (not the ID)
        bookService.validateIsbn(book.getIsbn());

        // Check borrow limit
        validateBorrowLimit(currentUser);

        // Check if book is available
        bookService.validateBookAvailability(book.getIsbn());

        // Check if user already borrowed this book
        if (isBookBorrowedByUser(bookId, currentUser)) {
            throw new BookAlreadyBorrowedException(book.getTitle());
        }

        // Create borrow record
        BorrowRecord record = BorrowRecord.builder()
                .user(currentUser)
                .book(book)
                .borrowDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(14))
                .build();

        // Decrease available copies
        bookService.decreaseAvailableCopies(book.getIsbn(), 1);

        BorrowRecord savedRecord = borrowRecordRepository.save(record);
        log.info("User {} borrowed book: {} (Due: {})",
                currentUser.getUsername(), book.getTitle(), savedRecord.getDueDate());
        return BorrowResponse.from(savedRecord);
    }

    @Override
    @Transactional
    public BorrowResponse returnBook(Long bookId) {
        User currentUser = getCurrentUser();
        Book book = bookService.getBookEntityById(bookId);

        // Find active borrow record
        BorrowRecord record = findActiveBorrowByBookAndUser(bookId, currentUser)
                .orElseThrow(() -> new BorrowRecordNotFoundException(
                        "No active borrow record found for book: " + book.getTitle()));

        // Calculate late fee (if any)
        BigDecimal lateFee = calculateLateFee(record);
        record.setLateFee(lateFee);
        record.setReturnDate(LocalDateTime.now());

        // Increase available copies
        bookService.increaseAvailableCopies(book.getIsbn(), 1);

        BorrowRecord savedRecord = borrowRecordRepository.save(record);
        log.info("User {} returned book: {} (Late Fee: {})",
                currentUser.getUsername(), book.getTitle(), lateFee);
        return BorrowResponse.from(savedRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowResponse> getUserBorrowRecords() {
        User currentUser = getCurrentUser();
        return borrowRecordRepository.findByUser(currentUser).stream()
                .map(BorrowResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowResponse> getAllBorrowRecords() {
        return borrowRecordRepository.findAll().stream()
                .map(BorrowResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public void validateBorrowLimit(User user) {
        long activeBorrows = borrowRecordRepository.countByUserAndReturnDateIsNull(user);
        if (activeBorrows >= 3) {
            throw new BorrowLimitExceededException(3);
        }
    }

    @Override
    public BigDecimal calculateLateFee(BorrowRecord record) {
        if (record.getReturnDate() == null) {
            return BigDecimal.ZERO;
        }
        long daysLate = ChronoUnit.DAYS.between(
                record.getDueDate(), record.getReturnDate());
        if (daysLate <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(daysLate).multiply(BigDecimal.ONE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean isBookBorrowedByUser(Long bookId, User user) {
        Book book = bookService.getBookEntityById(bookId);
        return borrowRecordRepository.findByUserAndBookAndReturnDateIsNull(user, book).isPresent();
    }

    @Override
    public Optional<BorrowRecord> findActiveBorrowByBookAndUser(Long bookId, User user) {
        Book book = bookService.getBookEntityById(bookId);
        return borrowRecordRepository.findByUserAndBookAndReturnDateIsNull(user, book);
    }
}