package com.lacunza.biblio.system.repository;

import com.lacunza.biblio.system.model.Book;
import com.lacunza.biblio.system.model.Loan;
import com.lacunza.biblio.system.model.Loan.LoanStatus;
import com.lacunza.biblio.system.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    // Buscar préstamos por usuario
    List<Loan> findByUser(User user);
    Page<Loan> findByUser(User user, Pageable pageable);
    
    // Buscar préstamos por libro
    List<Loan> findByBook(Book book);
    Page<Loan> findByBook(Book book, Pageable pageable);
    
    // Buscar préstamos por estado
    List<Loan> findByStatus(LoanStatus status);
    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);
    
    // Buscar préstamos por usuario y estado
    List<Loan> findByUserAndStatus(User user, LoanStatus status);
    Page<Loan> findByUserAndStatus(User user, LoanStatus status, Pageable pageable);
    
    // Buscar préstamos por usuario y múltiples estados
    List<Loan> findByUserAndStatusIn(User user, List<LoanStatus> statuses);
    
    // Contar préstamos por usuario y múltiples estados
    long countByUserAndStatusIn(User user, List<LoanStatus> statuses);
    
    // Buscar préstamos por libro y estado
    List<Loan> findByBookAndStatus(Book book, LoanStatus status);
    
    // Verificar si existe un préstamo activo para un libro y usuario
    boolean existsByBookAndUserAndStatus(Book book, User user, LoanStatus status);
    
    // Verificar si existe un préstamo activo para un libro
    boolean existsByBookAndStatusIn(Book book, List<LoanStatus> statuses);
    
    // Contar préstamos activos por libro
    long countByBookAndStatusIn(Book book, List<LoanStatus> statuses);
    
    // Buscar préstamos vencidos (fecha de vencimiento anterior a hoy y estado ACTIVE)
    List<Loan> findByDueDateBeforeAndStatus(LocalDate today, LoanStatus status);
    
    // Buscar préstamos que vencen pronto (en los próximos N días)
    @Query("SELECT l FROM Loan l WHERE l.dueDate BETWEEN :today AND :endDate AND l.status = :status")
    List<Loan> findLoansExpiringBetween(
            @Param("today") LocalDate today, 
            @Param("endDate") LocalDate endDate, 
            @Param("status") LoanStatus status);
    
    // Buscar préstamos por rango de fechas
    @Query("SELECT l FROM Loan l WHERE l.loanDate BETWEEN :startDate AND :endDate")
    List<Loan> findByLoanDateBetween(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    // Estadísticas: número de préstamos por mes/año
    @Query("SELECT FUNCTION('YEAR', l.loanDate) as year, FUNCTION('MONTH', l.loanDate) as month, COUNT(l) as count " +
           "FROM Loan l " +
           "WHERE l.loanDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('YEAR', l.loanDate), FUNCTION('MONTH', l.loanDate) " +
           "ORDER BY year, month")
    List<Object[]> getLoanStatsByMonth(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
}