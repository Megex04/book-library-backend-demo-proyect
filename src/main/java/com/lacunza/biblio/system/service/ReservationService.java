package com.lacunza.biblio.system.service;

import com.lacunza.biblio.system.dto.reservation.ReservationResponse;
import com.lacunza.biblio.system.exception.ResourceNotFoundException;
import com.lacunza.biblio.system.model.Book;
import com.lacunza.biblio.system.model.Reservation;
import com.lacunza.biblio.system.model.Reservation.ReservationStatus;
import com.lacunza.biblio.system.model.User;
import com.lacunza.biblio.system.repository.BookRepository;
import com.lacunza.biblio.system.repository.ReservationRepository;
import com.lacunza.biblio.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final LoanService loanService;

    /**
     * Crear una nueva reserva para un libro
     */
    @Transactional
    public ReservationResponse createReservationAsDTO(Long bookId, Long userId) {
        return convertToDTO(createReservation(bookId, userId));
    }

    @Transactional
    public Reservation createReservation(Long bookId, Long userId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Verificar si el usuario ya tiene una reserva activa para este libro
        boolean hasActiveReservation = reservationRepository.existsByBookAndUserAndStatusIn(
                book, 
                user, 
                Arrays.asList(ReservationStatus.PENDING, ReservationStatus.READY)
        );
        
        if (hasActiveReservation) {
            throw new IllegalStateException("El usuario ya tiene una reserva activa para este libro");
        }
        
        // Crear la reserva
        Reservation reservation = Reservation.builder()
                .book(book)
                .user(user)
                .reservationDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusMonths(1)) // La reserva expira en 1 mes si el libro no está disponible
                .status(ReservationStatus.PENDING)
                .notificationSent(false)
                .build();
        
        Reservation savedReservation = reservationRepository.save(reservation);
        
        // Enviar email de confirmación
        emailService.sendReservationConfirmationEmail(user.getEmail(), book.getTitle());
        
        // Si hay copias disponibles, procesar inmediatamente la reserva.
        // Importante: usamos el objeto devuelto por processReservation() (que sí
        // refleja el nuevo estado READY), en vez de savedReservation, que se
        // quedaría con status=PENDING en memoria aunque en BD ya esté en READY.
        if (book.getAvailableCopies() > 0) {
            return processReservation(savedReservation.getId());
        }

        return savedReservation;
    }

    /**
     * Procesar una reserva cuando un libro está disponible
     */
    @Transactional
    public ReservationResponse processReservationAsDTO(Long reservationId) {
        return convertToDTO(processReservation(reservationId));
    }

    @Transactional
    public Reservation processReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));
        
        Book book = reservation.getBook();
        
        // Verificar si hay copias disponibles
        if (book.getAvailableCopies() <= 0) {
            return reservation; // No hay copias disponibles, mantener en estado PENDING
        }
        
        // Actualizar la reserva a READY
        reservation.markAsReady(null); // El librarian se asignará cuando se entregue físicamente
        reservationRepository.save(reservation);
        
        // Enviar notificación al usuario
        emailService.sendReservationAvailableEmail(
                reservation.getUser().getEmail(), 
                book.getTitle(), 
                48 // Horas para retirar el libro
        );
        
        return reservation;
    }

    /**
     * Completar una reserva (entregar el libro al usuario)
     */
    @Transactional
    public ReservationResponse completeReservationAsDTO(Long reservationId, Long librarianId) {
        return convertToDTO(completeReservation(reservationId, librarianId));
    }

    @Transactional
    public Reservation completeReservation(Long reservationId, Long librarianId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        User librarian = userRepository.findById(librarianId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", librarianId));

        // Verificar que la reserva esté en estado READY
        if (reservation.getStatus() != ReservationStatus.READY) {
            throw new IllegalStateException("La reserva no está lista para ser completada");
        }

        // Actualizar la reserva
        reservation.complete(librarian);

        // Actualizar el libro (reducir copias disponibles)
        Book book = reservation.getBook();
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // Crear el préstamo real asociado a esta reserva. Antes de este fix,
        // completar una reserva descontaba la copia y marcaba la reserva como
        // COMPLETED pero nunca generaba un Loan: el libro salía del
        // inventario sin quedar ningún registro de préstamo (no aparecía en
        // "mis préstamos", no se podía devolver, no generaba multa por
        // atraso). createLoanFromReservation() NO vuelve a descontar la
        // copia (ya se descontó arriba), solo crea el registro de Loan.
        loanService.createLoanFromReservation(book, reservation.getUser(), librarian);

        return reservationRepository.save(reservation);
    }

    /**
     * Cancelar una reserva
     */
    @Transactional
    public ReservationResponse cancelReservationAsDTO(Long reservationId, Long librarianId, String reason) {
        return convertToDTO(cancelReservation(reservationId, librarianId, reason));
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId, Long librarianId, String reason) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));
        
        User librarian = null;
        if (librarianId != null) {
            librarian = userRepository.findById(librarianId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", librarianId));
        }
        
        // Verificar que la reserva esté en estado PENDING o READY
        if (reservation.getStatus() != ReservationStatus.PENDING && 
            reservation.getStatus() != ReservationStatus.READY) {
            throw new IllegalStateException("La reserva no puede ser cancelada en su estado actual");
        }
        
        // Actualizar la reserva
        reservation.cancel(librarian, reason);
        
        // Si la reserva estaba en estado READY, procesar la siguiente reserva pendiente
        if (reservation.getStatus() == ReservationStatus.READY) {
            processNextPendingReservation(reservation.getBook());
        }
        
        return reservationRepository.save(reservation);
    }

    /**
     * Procesar la siguiente reserva pendiente para un libro
     */
    @Transactional
    public void processNextPendingReservation(Book book) {
        List<Reservation> pendingReservations = reservationRepository.findNextPendingReservation(
                book, ReservationStatus.PENDING);
        
        if (!pendingReservations.isEmpty()) {
            Reservation nextReservation = pendingReservations.get(0);
            processReservation(nextReservation.getId());
        }
    }

    /**
     * Obtener todas las reservas de un usuario
     */
    @Transactional(readOnly = true)
    public List<Reservation> getUserReservations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return reservationRepository.findByUser(user);
    }

    /**
     * Obtener todas las reservas de un libro
     */
    @Transactional(readOnly = true)
    public List<Reservation> getBookReservations(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        return reservationRepository.findByBook(book);
    }

    /**
     * Obtener todas las reservas
     */
    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    /**
     * Obtener todas las reservas por estado
     */
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status);
    }

    /**
     * Obtener todas las reservas de un usuario ya convertidas a DTO.
     * El mapeo a DTO debe ocurrir dentro de la transacción (aquí, no en el
     * controller) porque accede a relaciones lazy (book, user, librarian).
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> getUserReservationsAsDTO(Long userId) {
        return getUserReservations(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todas las reservas de un libro ya convertidas a DTO.
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> getBookReservationsAsDTO(Long bookId) {
        return getBookReservations(bookId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todas las reservas (o filtradas por estado) ya convertidas a DTO.
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservationsAsDTO(ReservationStatus status) {
        List<Reservation> reservations = status != null
                ? getReservationsByStatus(status)
                : getAllReservations();

        return reservations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convertir una Reservation a su DTO de respuesta.
     * Debe invocarse siempre dentro de una transacción activa (por eso vive
     * en el service y no en el controller), ya que accede a relaciones lazy:
     * book, user y librarian.
     */
    public ReservationResponse convertToDTO(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .bookId(reservation.getBook().getId())
                .bookTitle(reservation.getBook().getTitle())
                .userId(reservation.getUser().getId())
                .userName(reservation.getUser().getFirstName() + " " + reservation.getUser().getLastName())
                .reservationDate(reservation.getReservationDate())
                .expiryDate(reservation.getExpiryDate())
                .status(reservation.getStatus())
                .notificationSent(reservation.isNotificationSent())
                .notes(reservation.getNotes())
                .librarianId(reservation.getLibrarian() != null ? reservation.getLibrarian().getId() : null)
                .librarianName(reservation.getLibrarian() != null ?
                        reservation.getLibrarian().getFirstName() + " " + reservation.getLibrarian().getLastName() : null)
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }

    /**
     * Tarea programada para marcar reservas expiradas
     * Se ejecuta todos los días a la medianoche
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void markExpiredReservations() {
        log.info("Ejecutando tarea programada para marcar reservas expiradas");
        
        List<Reservation> expiredReservations = reservationRepository.findByExpiryDateBeforeAndStatus(
                LocalDate.now(), ReservationStatus.READY);
        
        for (Reservation reservation : expiredReservations) {
            reservation.markAsExpired();
            reservationRepository.save(reservation);
            
            // Procesar la siguiente reserva pendiente para este libro
            processNextPendingReservation(reservation.getBook());
        }
        
        log.info("Se marcaron {} reservas como expiradas", expiredReservations.size());
    }

    /**
     * Tarea programada para enviar notificaciones de reservas listas
     * Se ejecuta cada hora
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void sendReservationNotifications() {
        log.info("Ejecutando tarea programada para enviar notificaciones de reservas");
        
        List<Reservation> reservationsToNotify = reservationRepository.findByStatusAndNotificationSent(
                ReservationStatus.READY, false);
        
        for (Reservation reservation : reservationsToNotify) {
            emailService.sendReservationAvailableEmail(
                    reservation.getUser().getEmail(),
                    reservation.getBook().getTitle(),
                    48 // Horas para retirar el libro
            );
            
            reservation.setNotificationSent(true);
            reservationRepository.save(reservation);
        }
        
        log.info("Se enviaron {} notificaciones de reservas", reservationsToNotify.size());
    }
}