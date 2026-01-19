package pe.com.lacunza.book.library.dto.loan;

import pe.com.lacunza.book.library.model.LoanStatus;

import java.time.LocalDate;

public record LoanResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Long userId,
        String userEmail,
        LocalDate loanDate,
        LocalDate returnDate,
        LocalDate actualReturnDate,
        LoanStatus status
) {}
