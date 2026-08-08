package com.lacunza.biblio.system.controller;

import com.lacunza.biblio.system.dto.reservation.ReservationCreateRequest;
import com.lacunza.biblio.system.dto.reservation.ReservationResponse;
import com.lacunza.biblio.system.dto.reservation.ReservationUpdateRequest;
import com.lacunza.biblio.system.model.Reservation.ReservationStatus;
import com.lacunza.biblio.system.model.User;
import com.lacunza.biblio.system.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Crear una nueva reserva (accesible para usuarios autenticados)
     */
    @PostMapping("/reservations")
    @PreAuthorize("hasAnyRole('MEMBER', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationCreateRequest request,
            @AuthenticationPrincipal User currentUser) {

        ReservationResponse response = reservationService.createReservationAsDTO(
                request.getBookId(),
                currentUser.getId()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Obtener todas las reservas del usuario actual
     */
    @GetMapping("/reservations/my")
    @PreAuthorize("hasAnyRole('MEMBER', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            @AuthenticationPrincipal User currentUser) {

        List<ReservationResponse> response = reservationService.getUserReservationsAsDTO(currentUser.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Cancelar una reserva del usuario actual
     */
    @PutMapping("/reservations/{id}/cancel")
    @PreAuthorize("hasAnyRole('MEMBER', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ReservationResponse> cancelMyReservation(
            @PathVariable Long id,
            @RequestBody(required = false) ReservationUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {

        String reason = (request != null) ? request.getReason() : "Cancelada por el usuario";

        ReservationResponse response = reservationService.cancelReservationAsDTO(id, currentUser.getId(), reason);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener todas las reservas (solo para bibliotecarios y administradores)
     */
    @GetMapping("/admin/reservations")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<List<ReservationResponse>> getAllReservations(
            @RequestParam(required = false) ReservationStatus status) {

        List<ReservationResponse> response = reservationService.getAllReservationsAsDTO(status);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener reservas por libro (solo para bibliotecarios y administradores)
     */
    @GetMapping("/admin/books/{bookId}/reservations")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<List<ReservationResponse>> getBookReservations(
            @PathVariable Long bookId) {

        List<ReservationResponse> response = reservationService.getBookReservationsAsDTO(bookId);

        return ResponseEntity.ok(response);
    }

    /**
     * Procesar una reserva (marcar como lista para retirar)
     * Solo para bibliotecarios y administradores
     */
    @PutMapping("/admin/reservations/{id}/process")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ReservationResponse> processReservation(
            @PathVariable Long id) {

        ReservationResponse response = reservationService.processReservationAsDTO(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Completar una reserva (entregar el libro)
     * Solo para bibliotecarios y administradores
     */
    @PutMapping("/admin/reservations/{id}/complete")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ReservationResponse> completeReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        ReservationResponse response = reservationService.completeReservationAsDTO(id, currentUser.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Cancelar una reserva (por un bibliotecario o administrador)
     */
    @PutMapping("/admin/reservations/{id}/cancel")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {

        ReservationResponse response = reservationService.cancelReservationAsDTO(
                id,
                currentUser.getId(),
                request.getReason()
        );

        return ResponseEntity.ok(response);
    }
}