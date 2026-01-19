package pe.com.lacunza.book.library.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pe.com.lacunza.book.library.dto.loan.LoanRequest;
import pe.com.lacunza.book.library.dto.loan.LoanResponse;
import pe.com.lacunza.book.library.model.User;
import pe.com.lacunza.book.library.service.LoanService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    // 1. Crear Préstamo (Solo personal autorizado)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<LoanResponse> createLoan(@RequestBody LoanRequest request) {
        return ResponseEntity.ok(loanService.createLoan(request));
    }

    // 2. Registrar Devolución (Solo personal autorizado)
    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<LoanResponse> returnLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.returnLoan(id));
    }

    // 3. Ver TODOS los préstamos (Solo personal autorizado)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<LoanResponse>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    // 4. Ver MIS préstamos (Cualquier usuario autenticado)
    @GetMapping("/my-loans")
    public ResponseEntity<List<LoanResponse>> getMyLoans() {
        // Obtenemos el usuario autenticado del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        return ResponseEntity.ok(loanService.getLoansByUserId(currentUser.getId()));
    }

    // 5. Ver préstamos de un usuario específico (Solo Admin/Librarian)
    // Útil para que el bibliotecario vea el historial de un cliente en el mostrador
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<LoanResponse>> getLoansByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(loanService.getLoansByUserId(userId));
    }
}
