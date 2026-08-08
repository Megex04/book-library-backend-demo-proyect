package com.lacunza.biblio.system.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Ver comentario equivalente en Book.java: sin esto, @Data genera hashCode/equals
// sobre las relaciones (loan, user, librarian), causando cargas lazy en cascada y
// ConcurrentModificationException bajo Hibernate.
@Entity
@Table(name = "fines")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "days_overdue", nullable = false)
    private Integer daysOverdue;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private FineStatus status;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

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

    // Enum para el estado de la multa
    public enum FineStatus {
        PENDING,    // Multa pendiente de pago
        PAID,       // Multa pagada
        WAIVED      // Multa condonada
    }

    // Método para marcar la multa como pagada
    public void markAsPaid(User librarian) {
        this.status = FineStatus.PAID;
        this.paymentDate = LocalDate.now();
        this.librarian = librarian;
    }

    // Método para condonar la multa
    public void waive(User librarian, String reason) {
        this.status = FineStatus.WAIVED;
        this.paymentDate = LocalDate.now();
        this.librarian = librarian;
        this.notes = reason;
    }
}