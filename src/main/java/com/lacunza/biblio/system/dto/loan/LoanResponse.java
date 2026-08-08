package com.lacunza.biblio.system.dto.loan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {
    
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long userId;
    private String userName;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String status;
    private Integer renewedCount;
    private String notes;
    
    // Información del bibliotecario
    private Long librarianId;
    private String librarianName;
    
    // Información de multa (si existe)
    private Long fineId;
    private BigDecimal fineAmount;
    private String fineStatus;
    
    // Información adicional para UI
    private Boolean overdue;
    private Integer daysOverdue;
    private Integer daysRemaining;
}