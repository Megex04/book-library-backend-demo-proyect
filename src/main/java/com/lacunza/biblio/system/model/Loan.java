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
// sobre las relaciones (book, user, fine), causando cargas lazy en cascada y
// ConcurrentModificationException bajo Hibernate.
@Entity
@Table(name = "loans")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "renewed_count", nullable = false)
    private Integer renewedCount;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    @OneToOne(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private Fine fine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "librarian_id")
    private User librarian;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "notes")
    private String notes;

    // Enum para el estado del préstamo
    public enum LoanStatus {
        ACTIVE,      // Préstamo activo
        RETURNED,    // Libro devuelto
        OVERDUE,     // Préstamo vencido
        LOST         // Libro perdido
    }

    // Método para renovar el préstamo
    public void renew(int daysToExtend) {
        this.dueDate = this.dueDate.plusDays(daysToExtend);
        this.renewedCount++;
    }

    // Método para devolver el libro
    public void returnBook(User librarian) {
        this.returnDate = LocalDate.now();
        this.status = LoanStatus.RETURNED;
        this.librarian = librarian;
    }

    // Método para verificar si el préstamo está vencido
    public boolean isOverdue() {
        return LocalDate.now().isAfter(this.dueDate) && !LoanStatus.RETURNED.equals(this.status);
    }

    // Método para calcular los días de retraso
    public long calculateDaysOverdue() {
        if (this.returnDate != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(this.dueDate, this.returnDate);
        } else {
            return java.time.temporal.ChronoUnit.DAYS.between(this.dueDate, LocalDate.now());
        }
    }
}