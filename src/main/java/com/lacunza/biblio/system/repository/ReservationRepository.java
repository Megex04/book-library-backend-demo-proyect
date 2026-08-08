package com.lacunza.biblio.system.repository;

import com.lacunza.biblio.system.model.Book;
import com.lacunza.biblio.system.model.Reservation;
import com.lacunza.biblio.system.model.Reservation.ReservationStatus;
import com.lacunza.biblio.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    // Buscar reservas por usuario
    List<Reservation> findByUser(User user);
    
    // Buscar reservas por libro
    List<Reservation> findByBook(Book book);
    
    // Buscar reservas por estado
    List<Reservation> findByStatus(ReservationStatus status);
    
    // Buscar reservas por usuario y estado
    List<Reservation> findByUserAndStatus(User user, ReservationStatus status);
    
    // Buscar reservas por usuario y lista de estados
    List<Reservation> findByUserAndStatusIn(User user, List<ReservationStatus> statuses);
    
    // Buscar reservas por libro y estado
    List<Reservation> findByBookAndStatus(Book book, ReservationStatus status);
    
    // Buscar reservas expiradas (fecha de expiración anterior a hoy y estado READY)
    List<Reservation> findByExpiryDateBeforeAndStatus(LocalDate today, ReservationStatus status);
    
    // Verificar si existe una reserva activa para un libro y usuario
    boolean existsByBookAndUserAndStatusIn(Book book, User user, List<ReservationStatus> statuses);
    
    // Verificar si existe una reserva para un libro con alguno de los estados especificados
    boolean existsByBookAndStatusIn(Book book, List<ReservationStatus> statuses);
    
    // Buscar la próxima reserva pendiente para un libro (ordenada por fecha de reserva)
    @Query("SELECT r FROM Reservation r WHERE r.book = ?1 AND r.status = ?2 ORDER BY r.reservationDate ASC")
    List<Reservation> findNextPendingReservation(Book book, ReservationStatus status);
    
    // Contar reservas activas por libro
    long countByBookAndStatusIn(Book book, List<ReservationStatus> statuses);
    
    // Buscar reservas que necesitan notificación (libro disponible pero no se ha enviado notificación)
    List<Reservation> findByStatusAndNotificationSent(ReservationStatus status, boolean notificationSent);
}