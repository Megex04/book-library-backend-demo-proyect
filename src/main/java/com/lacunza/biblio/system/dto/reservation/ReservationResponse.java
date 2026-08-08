package com.lacunza.biblio.system.dto.reservation;

import com.lacunza.biblio.system.model.Reservation.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    
    private Long id;
    
    private Long bookId;
    private String bookTitle;
    
    private Long userId;
    private String userName;
    
    private LocalDate reservationDate;
    private LocalDate expiryDate;
    
    private ReservationStatus status;
    private boolean notificationSent;
    
    private String notes;
    
    private Long librarianId;
    private String librarianName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}