package com.vbforge.libraryapi.service.impl;

import com.vbforge.libraryapi.dto.response.BorrowResponse;
import com.vbforge.libraryapi.entity.Book;
import com.vbforge.libraryapi.entity.BorrowRecord;
import com.vbforge.libraryapi.entity.Role;
import com.vbforge.libraryapi.entity.User;
import com.vbforge.libraryapi.exception.custom.*;
import com.vbforge.libraryapi.repository.BorrowRecordRepository;
import com.vbforge.libraryapi.repository.UserRepository;
import com.vbforge.libraryapi.service.BookService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BorrowServiceImpl Unit Tests")
class BorrowServiceImplTest {

    @Mock private BorrowRecordRepository borrowRecordRepository;
    @Mock private BookService bookService;
    @Mock private UserRepository userRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks private BorrowServiceImpl borrowService;

    private User currentUser;
    private Book sampleBook;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1L).username("johndoe")
                .email("john@example.com")
                .password("hashed")
                .role(Role.USER)
                .build();

        sampleBook = Book.builder()
                .id(1L).title("Clean Code")
                .author("Robert Martin")
                .isbn("9780132350884")
                .totalCopies(5).availableCopies(3)
                .build();

        // Wire up SecurityContext mock
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("johndoe");
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =========================================================
    //  borrowBook()
    // =========================================================
    @Nested
    @DisplayName("borrowBook()")
    class BorrowBookTests {

        @Test
        @DisplayName("should borrow a book successfully and return BorrowResponse")
        void borrowBook_success() {
            BorrowRecord savedRecord = BorrowRecord.builder()
                    .id(1L).user(currentUser).book(sampleBook)
                    .borrowDate(LocalDateTime.now())
                    .dueDate(LocalDateTime.now().plusDays(14))
                    .build();

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(currentUser));
            when(bookService.getBookEntityById(1L)).thenReturn(sampleBook);
            doNothing().when(bookService).validateIsbn(anyString());
            when(borrowRecordRepository.countByUserAndReturnDateIsNull(currentUser)).thenReturn(0L);
            doNothing().when(bookService).validateBookAvailability(anyString());
            when(borrowRecordRepository.findByUserAndBookAndReturnDateIsNull(currentUser, sampleBook))
                    .thenReturn(Optional.empty());
            doNothing().when(bookService).decreaseAvailableCopies(anyString(), anyInt());
            when(borrowRecordRepository.save(any())).thenReturn(savedRecord);

            BorrowResponse response = borrowService.borrowBook(1L);

            assertThat(response).isNotNull();
            assertThat(response.getBookTitle()).isEqualTo("Clean Code");
            verify(bookService).decreaseAvailableCopies("9780132350884", 1);
            verify(borrowRecordRepository).save(any());
        }

        @Test
        @DisplayName("should throw BorrowLimitExceededException when user has 3 active borrows")
        void borrowBook_limitExceeded() {
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(currentUser));
            when(bookService.getBookEntityById(1L)).thenReturn(sampleBook);
            doNothing().when(bookService).validateIsbn(anyString());
            when(borrowRecordRepository.countByUserAndReturnDateIsNull(currentUser)).thenReturn(3L);

            assertThatThrownBy(() -> borrowService.borrowBook(1L))
                    .isInstanceOf(BorrowLimitExceededException.class);

            verify(borrowRecordRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw BookAlreadyBorrowedException when user already borrowed this book")
        void borrowBook_alreadyBorrowed() {
            BorrowRecord existingRecord = BorrowRecord.builder()
                    .user(currentUser).book(sampleBook).build();

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(currentUser));
            when(bookService.getBookEntityById(1L)).thenReturn(sampleBook);
            doNothing().when(bookService).validateIsbn(anyString());
            when(borrowRecordRepository.countByUserAndReturnDateIsNull(currentUser)).thenReturn(1L);
            doNothing().when(bookService).validateBookAvailability(anyString());
            when(borrowRecordRepository.findByUserAndBookAndReturnDateIsNull(currentUser, sampleBook))
                    .thenReturn(Optional.of(existingRecord));

            assertThatThrownBy(() -> borrowService.borrowBook(1L))
                    .isInstanceOf(BookAlreadyBorrowedException.class);
        }

        @Test
        @DisplayName("should set dueDate to 14 days from borrow date")
        void borrowBook_setsDueDateCorrectly() {
            BorrowRecord savedRecord = BorrowRecord.builder()
                    .id(1L).user(currentUser).book(sampleBook)
                    .borrowDate(LocalDateTime.now())
                    .dueDate(LocalDateTime.now().plusDays(14))
                    .build();

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(currentUser));
            when(bookService.getBookEntityById(1L)).thenReturn(sampleBook);
            doNothing().when(bookService).validateIsbn(anyString());
            when(borrowRecordRepository.countByUserAndReturnDateIsNull(currentUser)).thenReturn(0L);
            doNothing().when(bookService).validateBookAvailability(anyString());
            when(borrowRecordRepository.findByUserAndBookAndReturnDateIsNull(any(), any()))
                    .thenReturn(Optional.empty());
            doNothing().when(bookService).decreaseAvailableCopies(anyString(), anyInt());
            when(borrowRecordRepository.save(any())).thenReturn(savedRecord);

            BorrowResponse response = borrowService.borrowBook(1L);

            // Due date should be roughly 14 days from now
            assertThat(response.getDueDate()).isAfter(LocalDateTime.now().plusDays(13));
        }
    }

    // =========================================================
    //  returnBook()
    // =========================================================
    @Nested
    @DisplayName("returnBook()")
    class ReturnBookTests {

        @Test
        @DisplayName("should return book successfully with no late fee")
        void returnBook_onTime_noLateFee() {
            BorrowRecord activeRecord = BorrowRecord.builder()
                    .id(1L).user(currentUser).book(sampleBook)
                    .borrowDate(LocalDateTime.now().minusDays(5))
                    .dueDate(LocalDateTime.now().plusDays(9))
                    .build();

            BorrowRecord returnedRecord = BorrowRecord.builder()
                    .id(1L).user(currentUser).book(sampleBook)
                    .borrowDate(activeRecord.getBorrowDate())
                    .dueDate(activeRecord.getDueDate())
                    .returnDate(LocalDateTime.now())
                    .lateFee(BigDecimal.ZERO)
                    .build();

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(currentUser));
            when(bookService.getBookEntityById(1L)).thenReturn(sampleBook);
            when(borrowRecordRepository.findByUserAndBookAndReturnDateIsNull(currentUser, sampleBook))
                    .thenReturn(Optional.of(activeRecord));
            doNothing().when(bookService).increaseAvailableCopies(anyString(), anyInt());
            when(borrowRecordRepository.save(any())).thenReturn(returnedRecord);

            BorrowResponse response = borrowService.returnBook(1L);

            assertThat(response.getLateFee()).isEqualByComparingTo(BigDecimal.ZERO);
            verify(bookService).increaseAvailableCopies("9780132350884", 1);
        }

        @Test
        @DisplayName("should calculate late fee for overdue return")
        void returnBook_late_chargesLateFee() {
            LocalDateTime dueDate = LocalDateTime.now().minusDays(3);
            BorrowRecord activeRecord = BorrowRecord.builder()
                    .id(1L).user(currentUser).book(sampleBook)
                    .borrowDate(LocalDateTime.now().minusDays(17))
                    .dueDate(dueDate)
                    .build();

            BorrowRecord returnedRecord = BorrowRecord.builder()
                    .id(1L).user(currentUser).book(sampleBook)
                    .borrowDate(activeRecord.getBorrowDate())
                    .dueDate(dueDate)
                    .returnDate(LocalDateTime.now())
                    .lateFee(new BigDecimal("3.00"))
                    .build();

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(currentUser));
            when(bookService.getBookEntityById(1L)).thenReturn(sampleBook);
            when(borrowRecordRepository.findByUserAndBookAndReturnDateIsNull(currentUser, sampleBook))
                    .thenReturn(Optional.of(activeRecord));
            doNothing().when(bookService).increaseAvailableCopies(anyString(), anyInt());
            when(borrowRecordRepository.save(any())).thenReturn(returnedRecord);

            BorrowResponse response = borrowService.returnBook(1L);

            assertThat(response.getLateFee()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should throw BorrowRecordNotFoundException when no active borrow exists")
        void returnBook_noActiveBorrow() {
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(currentUser));
            when(bookService.getBookEntityById(1L)).thenReturn(sampleBook);
            when(borrowRecordRepository.findByUserAndBookAndReturnDateIsNull(currentUser, sampleBook))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> borrowService.returnBook(1L))
                    .isInstanceOf(BorrowRecordNotFoundException.class);

            verify(bookService, never()).increaseAvailableCopies(anyString(), anyInt());
        }
    }

    // =========================================================
    //  calculateLateFee()
    // =========================================================
    @Nested
    @DisplayName("calculateLateFee()")
    class CalculateLateFeeTests {

        @Test
        @DisplayName("should return ZERO when returnDate is null")
        void calculateLateFee_nullReturnDate() {
            BorrowRecord record = BorrowRecord.builder()
                    .dueDate(LocalDateTime.now().plusDays(5))
                    .returnDate(null)
                    .build();

            assertThat(borrowService.calculateLateFee(record))
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should return ZERO when returned on time")
        void calculateLateFee_onTime() {
            BorrowRecord record = BorrowRecord.builder()
                    .dueDate(LocalDateTime.now())
                    .returnDate(LocalDateTime.now().minusHours(1))
                    .build();

            assertThat(borrowService.calculateLateFee(record))
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should return $1 per day late")
        void calculateLateFee_perDayCharge() {
            LocalDateTime dueDate = LocalDateTime.now().minusDays(5);
            BorrowRecord record = BorrowRecord.builder()
                    .dueDate(dueDate)
                    .returnDate(LocalDateTime.now())
                    .build();

            BigDecimal fee = borrowService.calculateLateFee(record);

            assertThat(fee).isEqualByComparingTo(new BigDecimal("5.00"));
        }
    }

    // =========================================================
    //  validateBorrowLimit()
    // =========================================================
    @Nested
    @DisplayName("validateBorrowLimit()")
    class ValidateBorrowLimitTests {

        @Test
        @DisplayName("should not throw when user has fewer than 3 active borrows")
        void validateBorrowLimit_underLimit() {
            when(borrowRecordRepository.countByUserAndReturnDateIsNull(currentUser)).thenReturn(2L);

            assertThatCode(() -> borrowService.validateBorrowLimit(currentUser))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw BorrowLimitExceededException when at limit of 3")
        void validateBorrowLimit_atLimit() {
            when(borrowRecordRepository.countByUserAndReturnDateIsNull(currentUser)).thenReturn(3L);

            assertThatThrownBy(() -> borrowService.validateBorrowLimit(currentUser))
                    .isInstanceOf(BorrowLimitExceededException.class);
        }

        @Test
        @DisplayName("should throw BorrowLimitExceededException when above limit")
        void validateBorrowLimit_aboveLimit() {
            when(borrowRecordRepository.countByUserAndReturnDateIsNull(currentUser)).thenReturn(5L);

            assertThatThrownBy(() -> borrowService.validateBorrowLimit(currentUser))
                    .isInstanceOf(BorrowLimitExceededException.class);
        }
    }

    // =========================================================
    //  getUserBorrowRecords()
    // =========================================================
    @Nested
    @DisplayName("getUserBorrowRecords()")
    class GetUserBorrowRecordsTests {

        @Test
        @DisplayName("should return all borrow records for current user")
        void getUserBorrowRecords_success() {
            BorrowRecord record = BorrowRecord.builder()
                    .id(1L).user(currentUser).book(sampleBook)
                    .borrowDate(LocalDateTime.now().minusDays(5))
                    .dueDate(LocalDateTime.now().plusDays(9))
                    .build();

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(currentUser));
            when(borrowRecordRepository.findByUser(currentUser)).thenReturn(List.of(record));

            List<BorrowResponse> records = borrowService.getUserBorrowRecords();

            assertThat(records).hasSize(1);
            assertThat(records.get(0).getBookTitle()).isEqualTo("Clean Code");
        }

        @Test
        @DisplayName("should return empty list when user has no borrow records")
        void getUserBorrowRecords_empty() {
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(currentUser));
            when(borrowRecordRepository.findByUser(currentUser)).thenReturn(List.of());

            List<BorrowResponse> records = borrowService.getUserBorrowRecords();

            assertThat(records).isEmpty();
        }
    }
}
