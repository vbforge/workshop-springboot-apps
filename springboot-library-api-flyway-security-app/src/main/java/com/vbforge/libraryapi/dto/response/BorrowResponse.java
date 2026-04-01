package com.vbforge.libraryapi.dto.response;

import com.vbforge.libraryapi.entity.BorrowRecord;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class BorrowResponse {
    private Long id;
    private BookResponse book;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private BigDecimal lateFee;

    public static BorrowResponse from(BorrowRecord record) {
        return BorrowResponse.builder()
                .id(record.getId())
                .book(BookResponse.from(record.getBook()))
                .borrowDate(record.getBorrowDate())
                .dueDate(record.getDueDate())
                .returnDate(record.getReturnDate())
                .lateFee(record.getLateFee())
                .build();
    }

    public String getBookTitle(){
        return this.book.getTitle();
    }

}