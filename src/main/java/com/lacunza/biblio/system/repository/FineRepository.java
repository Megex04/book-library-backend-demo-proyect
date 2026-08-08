package com.lacunza.biblio.system.repository;

import com.lacunza.biblio.system.model.Fine;
import com.lacunza.biblio.system.model.Fine.FineStatus;
import com.lacunza.biblio.system.model.Loan;
import com.lacunza.biblio.system.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    
    // Buscar multas por usuario
    List<Fine> findByUser(User user);
    Page<Fine> findByUser(User user, Pageable pageable);
    
    // Buscar multas por préstamo
    Fine findByLoan(Loan loan);
    
    // Buscar multas por estado
    List<Fine> findByStatus(FineStatus status);
    Page<Fine> findByStatus(FineStatus status, Pageable pageable);
    
    // Buscar multas por usuario y estado
    List<Fine> findByUserAndStatus(User user, FineStatus status);
    Page<Fine> findByUserAndStatus(User user, FineStatus status, Pageable pageable);
    
    // Contar multas por usuario y estado
    long countByUserAndStatus(User user, FineStatus status);
    
    // Buscar multas por usuario y estado diferente
    List<Fine> findByUserAndStatusNot(User user, FineStatus status);
    
    // Buscar multas por rango de fechas de creación
    @Query("SELECT f FROM Fine f WHERE f.createdAt BETWEEN :startDate AND :endDate")
    List<Fine> findByCreatedAtBetween(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    // Buscar multas por rango de fechas de pago
    @Query("SELECT f FROM Fine f WHERE f.paymentDate BETWEEN :startDate AND :endDate")
    List<Fine> findByPaymentDateBetween(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    // Buscar multas por monto mayor que
    List<Fine> findByAmountGreaterThan(BigDecimal amount);
    
    // Buscar multas por monto menor que
    List<Fine> findByAmountLessThan(BigDecimal amount);
    
    // Buscar multas por rango de monto
    @Query("SELECT f FROM Fine f WHERE f.amount BETWEEN :minAmount AND :maxAmount")
    List<Fine> findByAmountBetween(
            @Param("minAmount") BigDecimal minAmount, 
            @Param("maxAmount") BigDecimal maxAmount);
    
    // Calcular el total de multas por usuario
    @Query("SELECT SUM(f.amount) FROM Fine f WHERE f.user = :user AND f.status = :status")
    BigDecimal calculateTotalFinesByUserAndStatus(
            @Param("user") User user, 
            @Param("status") FineStatus status);
    
    // Calcular el total de multas por período
    @Query("SELECT SUM(f.amount) FROM Fine f WHERE f.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalFinesByPeriod(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    // Estadísticas: número de multas por mes/año
    @Query("SELECT FUNCTION('YEAR', f.createdAt) as year, FUNCTION('MONTH', f.createdAt) as month, COUNT(f) as count, SUM(f.amount) as total " +
           "FROM Fine f " +
           "WHERE f.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('YEAR', f.createdAt), FUNCTION('MONTH', f.createdAt) " +
           "ORDER BY year, month")
    List<Object[]> getFineStatsByMonth(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
}