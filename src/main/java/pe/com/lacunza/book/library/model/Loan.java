package pe.com.lacunza.book.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con Usuario (Quién pide)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relación con Libro (Qué pide)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private LocalDate loanDate; // Fecha de inicio

    @Column(nullable = false)
    private LocalDate returnDate; // Fecha límite de devolución

    private LocalDate actualReturnDate; // Fecha real cuando lo devuelve

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    // Método auxiliar para saber si está vencido hoy
    public boolean isOverdue() {
        return LoanStatus.ACTIVE.equals(this.status) && LocalDate.now().isAfter(this.returnDate);
    }
}
