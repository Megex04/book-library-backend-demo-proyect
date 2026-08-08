package com.lacunza.biblio.system.dto.fine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FineResponse {
    
    private Long id;
    private Long loanId;
    private Long bookId;
    private String bookTitle;
    private Long userId;
    private String userName;
    private BigDecimal amount;
    private Integer daysOverdue;
    private String status;
    private LocalDate paymentDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Información del bibliotecario
    private Long librarianId;
    private String librarianName;
}