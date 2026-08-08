package com.lacunza.biblio.system.controller;

import com.lacunza.biblio.system.dto.fine.FineResponse;
import com.lacunza.biblio.system.dto.fine.FineUpdateRequest;
import com.lacunza.biblio.system.model.User;
import com.lacunza.biblio.system.service.FineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FineController {

    private final FineService fineService;

    /**
     * Obtener multas del usuario actual
     * Accesible para usuarios autenticados
     */
    @GetMapping("/fines/my")
    public ResponseEntity<Page<FineResponse>> getMyFines(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        
        Page<FineResponse> fines = fineService.getFinesByUser(currentUser.getId(), status, pageable);
        return ResponseEntity.ok(fines);
    }

    /**
     * Obtener todas las multas (para administradores y bibliotecarios)
     */
    @GetMapping("/admin/fines")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Page<FineResponse>> getAllFines(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        
        Page<FineResponse> fines = fineService.getAllFines(status, pageable);
        return ResponseEntity.ok(fines);
    }

    /**
     * Obtener multas por usuario (para administradores y bibliotecarios)
     */
    @GetMapping("/admin/fines/user/{userId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Page<FineResponse>> getFinesByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        
        Page<FineResponse> fines = fineService.getFinesByUser(userId, status, pageable);
        return ResponseEntity.ok(fines);
    }

    /**
     * Obtener una multa por ID
     */
    @GetMapping("/fines/{id}")
    public ResponseEntity<FineResponse> getFineById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        try {
            FineResponse fine = fineService.getFineById(id, currentUser);
            return ResponseEntity.ok(fine);
        } catch (AccessDeniedException e) {
            throw new AccessDeniedException("Access denied to fine with id: " + id);
        }
    }

    /**
     * Marcar una multa como pagada
     * Solo para bibliotecarios y administradores
     */
    @PostMapping("/admin/fines/{id}/pay")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<FineResponse> markFineAsPaid(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) FineUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        FineResponse paidFine = fineService.markFineAsPaid(id, request, currentUser);
        return ResponseEntity.ok(paidFine);
    }

    /**
     * Condonar una multa
     * Solo para administradores
     */
    @PostMapping("/admin/fines/{id}/waive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FineResponse> waiveFine(
            @PathVariable Long id,
            @Valid @RequestBody FineUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        FineResponse waivedFine = fineService.waiveFine(id, request, currentUser);
        return ResponseEntity.ok(waivedFine);
    }

    /**
     * Obtener estadísticas de multas por período
     * Solo para administradores
     */
    @GetMapping("/admin/fines/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getFineStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<Object[]> stats = fineService.getFineStatsByPeriod(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    /**
     * Obtener el total de multas pendientes
     * Solo para administradores
     */
    @GetMapping("/admin/fines/total-pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BigDecimal> getTotalPendingFines() {
        BigDecimal total = fineService.calculateTotalPendingFines();
        return ResponseEntity.ok(total);
    }

    /**
     * Obtener el total de multas pagadas en un período
     * Solo para administradores
     */
    @GetMapping("/admin/fines/total-paid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BigDecimal> getTotalPaidFines(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        BigDecimal total = fineService.calculateTotalPaidFinesByPeriod(startDate, endDate);
        return ResponseEntity.ok(total);
    }
}