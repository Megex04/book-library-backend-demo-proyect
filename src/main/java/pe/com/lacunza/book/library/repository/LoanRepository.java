package pe.com.lacunza.book.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.com.lacunza.book.library.model.Loan;
import pe.com.lacunza.book.library.model.LoanStatus;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    // Buscar préstamos de un usuario específico
    List<Loan> findByUserId(Long userId);

    // Buscar préstamos activos de un usuario (para validar límite de libros)
    List<Loan> findByUserIdAndStatus(Long userId, LoanStatus status);

    // Validar si un usuario tiene libros vencidos
    @Query("SELECT COUNT(l) > 0 FROM Loan l WHERE l.user.id = :userId AND l.status = 'ACTIVE' AND l.returnDate < CURRENT_DATE")
    boolean hasOverdueLoans(Long userId);

    // Buscar todos los préstamos activos (útil para reportes o jobs automáticos)
    List<Loan> findByStatus(LoanStatus status);
}
