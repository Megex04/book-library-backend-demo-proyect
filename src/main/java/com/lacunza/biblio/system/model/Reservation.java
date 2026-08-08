package com.lacunza.biblio.system.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Ver comentario equivalente en Book.java: sin esto, @Data genera hashCode/equals
// sobre las relaciones (book, user, librarian), causando cargas lazy en cascada y
// ConcurrentModificationException bajo Hibernate.
@Entity
@Table(name = "reservations")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "notification_sent", nullable = false)
    private boolean notificationSent;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "librarian_id")
    private User librarian;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enum para el estado de la reserva
    public enum ReservationStatus {
        PENDING,    // Reserva pendiente (esperando que el libro esté disponible)
        READY,      // Libro disponible para retirar
        COMPLETED,  // Reserva completada (libro retirado)
        EXPIRED,    // Reserva expirada (no se retiró el libro en el tiempo establecido)
        CANCELLED   // Reserva cancelada por el usuario o bibliotecario
    }

    // Método para marcar la reserva como lista para retirar
    public void markAsReady(User librarian) {
        this.status = ReservationStatus.READY;
        this.librarian = librarian;
        this.notificationSent = true;
        // La fecha de expiración se establece típicamente a 24-48 horas después
        this.expiryDate = LocalDate.now().plusDays(2);
    }

    // Método para completar la reserva (libro retirado)
    public void complete(User librarian) {
        this.status = ReservationStatus.COMPLETED;
        this.librarian = librarian;
    }

    // Método para cancelar la reserva
    public void cancel(User librarian, String reason) {
        this.status = ReservationStatus.CANCELLED;
        this.librarian = librarian;
        this.notes = reason;
    }

    // Método para verificar si la reserva ha expirado
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    // Método para marcar la reserva como expirada
    public void markAsExpired() {
        this.status = ReservationStatus.EXPIRED;
    }
}