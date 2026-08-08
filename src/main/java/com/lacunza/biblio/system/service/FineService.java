package com.lacunza.biblio.system.service;

import com.lacunza.biblio.system.dto.fine.FineResponse;
import com.lacunza.biblio.system.dto.fine.FineUpdateRequest;
import com.lacunza.biblio.system.exception.ResourceNotFoundException;
import com.lacunza.biblio.system.model.Fine;
import com.lacunza.biblio.system.model.Fine.FineStatus;
import com.lacunza.biblio.system.model.User;
import com.lacunza.biblio.system.repository.FineRepository;
import com.lacunza.biblio.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FineService {

    private final FineRepository fineRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * Obtener todas las multas
     */
    @Transactional(readOnly = true)
    public Page<FineResponse> getAllFines(String status, Pageable pageable) {
        Page<Fine> finesPage;
        
        if (status != null && !status.isEmpty()) {
            try {
                FineStatus fineStatus = FineStatus.valueOf(status.toUpperCase());
                finesPage = fineRepository.findByStatus(fineStatus, pageable);
            } catch (IllegalArgumentException e) {
                // Si el estado no es válido, devolver todas las multas
                finesPage = fineRepository.findAll(pageable);
            }
        } else {
            finesPage = fineRepository.findAll(pageable);
        }
        
        return finesPage.map(this::convertToResponse);
    }

    /**
     * Obtener multas por usuario
     */
    @Transactional(readOnly = true)
    public Page<FineResponse> getFinesByUser(Long userId, String status, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
        
        Page<Fine> finesPage;
        
        if (status != null && !status.isEmpty()) {
            try {
                FineStatus fineStatus = FineStatus.valueOf(status.toUpperCase());
                finesPage = fineRepository.findByUserAndStatus(user, fineStatus, pageable);
            } catch (IllegalArgumentException e) {
                // Si el estado no es válido, devolver todas las multas del usuario
                finesPage = fineRepository.findByUser(user, pageable);
            }
        } else {
            finesPage = fineRepository.findByUser(user, pageable);
        }
        
        return finesPage.map(this::convertToResponse);
    }

    /**
     * Obtener una multa por ID
     * @throws AccessDeniedException 
     */
    @Transactional(readOnly = true)
    public FineResponse getFineById(Long id, User currentUser) throws AccessDeniedException {
        Fine fine = findFineById(id);
        
        // Verificar permisos: solo el usuario de la multa, bibliotecarios o administradores pueden ver los detalles
        if (!fine.getUser().getId().equals(currentUser.getId()) && 
                !currentUser.hasAnyRole("LIBRARIAN", "ADMIN")) {
            throw new AccessDeniedException("No tienes permiso para ver esta multa");
        }
        
        return convertToResponse(fine);
    }

    /**
     * Marcar una multa como pagada
     */
    @Transactional
    public FineResponse markFineAsPaid(Long id, FineUpdateRequest request, User librarian) {
        Fine fine = findFineById(id);
        
        // Verificar si la multa está pendiente
        if (fine.getStatus() != FineStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden marcar como pagadas las multas pendientes");
        }
        
        // Marcar la multa como pagada
        fine.markAsPaid(librarian);
        
        // Agregar notas si se proporcionan
        if (request != null && request.getNotes() != null) {
            fine.setNotes(request.getNotes());
        }
        
        // Actualizar fecha de modificación
        fine.setUpdatedAt(LocalDateTime.now());
        
        Fine paidFine = fineRepository.save(fine);
        
        // Enviar correo de confirmación al usuario
        sendFinePaymentConfirmationEmail(paidFine);
        
        return convertToResponse(paidFine);
    }

    /**
     * Condonar una multa
     */
    @Transactional
    public FineResponse waiveFine(Long id, FineUpdateRequest request, User admin) {
        Fine fine = findFineById(id);
        
        // Verificar si la multa está pendiente
        if (fine.getStatus() != FineStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden condonar las multas pendientes");
        }
        
        // Verificar que se proporcione una razón para la condonación
        if (request == null || request.getNotes() == null || request.getNotes().trim().isEmpty()) {
            throw new IllegalArgumentException("Se debe proporcionar una razón para condonar la multa");
        }
        
        // Condonar la multa
        fine.waive(admin, request.getNotes());
        
        // Actualizar fecha de modificación
        fine.setUpdatedAt(LocalDateTime.now());
        
        Fine waivedFine = fineRepository.save(fine);
        
        // Enviar correo de notificación al usuario
        sendFineWaivedNotificationEmail(waivedFine);
        
        return convertToResponse(waivedFine);
    }

    /**
     * Obtener estadísticas de multas por período
     */
    public List<Object[]> getFineStatsByPeriod(LocalDate startDate, LocalDate endDate) {
        return fineRepository.getFineStatsByMonth(startDate, endDate);
    }

    /**
     * Calcular el total de multas pendientes
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalPendingFines() {
        List<Fine> pendingFines = fineRepository.findByStatus(FineStatus.PENDING);
        
        return pendingFines.stream()
                .map(Fine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcular el total de multas pagadas en un período
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalPaidFinesByPeriod(LocalDate startDate, LocalDate endDate) {
        List<Fine> paidFines = fineRepository.findByPaymentDateBetween(startDate, endDate);
        
        return paidFines.stream()
                .filter(fine -> fine.getStatus() == FineStatus.PAID)
                .map(Fine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Método auxiliar para buscar una multa por ID
     */
    private Fine findFineById(Long id) {
        return fineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Multa no encontrada con ID: " + id));
    }

    /**
     * Método auxiliar para convertir un Fine a FineResponse
     */
    private FineResponse convertToResponse(Fine fine) {
        FineResponse response = new FineResponse();
        response.setId(fine.getId());
        response.setLoanId(fine.getLoan().getId());
        response.setBookId(fine.getLoan().getBook().getId());
        response.setBookTitle(fine.getLoan().getBook().getTitle());
        response.setUserId(fine.getUser().getId());
        response.setUserName(fine.getUser().getFirstName() + " " + fine.getUser().getLastName());
        response.setAmount(fine.getAmount());
        response.setDaysOverdue(fine.getDaysOverdue());
        response.setStatus(fine.getStatus().name());
        response.setPaymentDate(fine.getPaymentDate());
        response.setNotes(fine.getNotes());
        response.setCreatedAt(fine.getCreatedAt());
        response.setUpdatedAt(fine.getUpdatedAt());
        
        // Agregar información del bibliotecario si existe
        if (fine.getLibrarian() != null) {
            response.setLibrarianId(fine.getLibrarian().getId());
            response.setLibrarianName(fine.getLibrarian().getFirstName() + " " + fine.getUser().getLastName());
        }
        
        return response;
    }

    /**
     * Métodos para enviar correos electrónicos
     */
    private void sendFinePaymentConfirmationEmail(Fine fine) {
        String subject = "Confirmación de pago de multa - Biblioteca";
        String content = String.format(
                "Estimado/a %s,\n\n" +
                "Le confirmamos que ha pagado la multa correspondiente al préstamo del libro '%s'.\n" +
                "Monto pagado: $%s\n" +
                "Fecha de pago: %s\n\n" +
                "Gracias por utilizar nuestros servicios.\n\n" +
                "Saludos cordiales,\n" +
                "El equipo de la Biblioteca",
                fine.getUser().getFirstName() + " " + fine.getUser().getLastName(),
                fine.getLoan().getBook().getTitle(),
                fine.getAmount().toString(),
                fine.getPaymentDate());
        
        emailService.sendEmail(fine.getUser().getEmail(), subject, content);
    }

    private void sendFineWaivedNotificationEmail(Fine fine) {
        String subject = "Notificación de multa condonada - Biblioteca";
        String content = String.format(
                "Estimado/a %s,\n\n" +
                "Le informamos que la multa correspondiente al préstamo del libro '%s' ha sido condonada.\n" +
                "Monto condonado: $%s\n" +
                "Razón: %s\n\n" +
                "Gracias por utilizar nuestros servicios.\n\n" +
                "Saludos cordiales,\n" +
                "El equipo de la Biblioteca",
                fine.getUser().getFirstName() + " " + fine.getUser().getLastName(),
                fine.getLoan().getBook().getTitle(),
                fine.getAmount().toString(),
                fine.getNotes());
        
        emailService.sendEmail(fine.getUser().getEmail(), subject, content);
    }
}