package com.lacunza.biblio.system.controller;

import com.lacunza.biblio.system.dto.loan.LoanCreateRequest;
import com.lacunza.biblio.system.dto.loan.LoanResponse;
import com.lacunza.biblio.system.dto.loan.LoanUpdateRequest;
import com.lacunza.biblio.system.model.User;
import com.lacunza.biblio.system.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    /**
     * Obtener préstamos del usuario actual
     * Accesible para usuarios autenticados
     */
    @GetMapping("/loans/my")
    public ResponseEntity<Page<LoanResponse>> getMyLoans(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "loanDate") Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        
        Page<LoanResponse> loans = loanService.getLoansByUser(currentUser.getId(), status, pageable);
        return ResponseEntity.ok(loans);
    }

    /**
     * Obtener todos los préstamos (para administradores y bibliotecarios)
     */
    @GetMapping("/admin/loans")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Page<LoanResponse>> getAllLoans(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "loanDate") Pageable pageable) {
        
        Page<LoanResponse> loans = loanService.getAllLoans(status, pageable);
        return ResponseEntity.ok(loans);
    }

    /**
     * Obtener préstamos por usuario (para administradores y bibliotecarios)
     */
    @GetMapping("/admin/loans/user/{userId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Page<LoanResponse>> getLoansByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "loanDate") Pageable pageable) {
        
        Page<LoanResponse> loans = loanService.getLoansByUser(userId, status, pageable);
        return ResponseEntity.ok(loans);
    }

    /**
     * Obtener préstamos por libro (para administradores y bibliotecarios)
     */
    @GetMapping("/admin/loans/book/{bookId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Page<LoanResponse>> getLoansByBook(
            @PathVariable Long bookId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "loanDate") Pageable pageable) {
        
        Page<LoanResponse> loans = loanService.getLoansByBook(bookId, status, pageable);
        return ResponseEntity.ok(loans);
    }

    /**
     * Obtener un préstamo por ID
     */
    @GetMapping("/loans/{id}")
    public ResponseEntity<LoanResponse> getLoanById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        LoanResponse loan = loanService.getLoanById(id, currentUser);
        return ResponseEntity.ok(loan);
    }

    /**
     * Crear un nuevo préstamo (solo bibliotecarios y administradores)
     */
    @PostMapping("/admin/loans")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<LoanResponse> createLoan(
            @Valid @RequestBody LoanCreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        LoanResponse createdLoan = loanService.createLoan(request, currentUser);
        return new ResponseEntity<>(createdLoan, HttpStatus.CREATED);
    }

    /**
     * Renovar un préstamo
     */
    @PostMapping("/loans/{id}/renew")
    public ResponseEntity<LoanResponse> renewLoan(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        LoanResponse renewedLoan = loanService.renewLoan(id, currentUser);
        return ResponseEntity.ok(renewedLoan);
    }

    /**
     * Devolver un libro (marcar préstamo como devuelto)
     */
    @PostMapping("/admin/loans/{id}/return")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<LoanResponse> returnBook(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) LoanUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        LoanResponse returnedLoan = loanService.returnBook(id, request, currentUser);
        return ResponseEntity.ok(returnedLoan);
    }

    /**
     * Buscar préstamos vencidos
     */
    @GetMapping("/admin/loans/overdue")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Page<LoanResponse>> getOverdueLoans(
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) {
        
        Page<LoanResponse> overdueLoans = loanService.getOverdueLoans(pageable);
        return ResponseEntity.ok(overdueLoans);
    }

    /**
     * Buscar préstamos que vencen pronto
     */
    @GetMapping("/admin/loans/expiring-soon")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Page<LoanResponse>> getLoansExpiringSoon(
            @RequestParam(defaultValue = "7") int days,
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) {
        
        Page<LoanResponse> expiringLoans = loanService.getLoansExpiringSoon(days, pageable);
        return ResponseEntity.ok(expiringLoans);
    }

    /**
     * Obtener estadísticas de préstamos por período
     */
    @GetMapping("/admin/loans/stats")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<List<Object[]>> getLoanStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<Object[]> stats = loanService.getLoanStatsByPeriod(startDate, endDate);
        return ResponseEntity.ok(stats);
    }
}