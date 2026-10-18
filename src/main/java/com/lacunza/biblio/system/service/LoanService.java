package com.lacunza.biblio.system.service;

import com.lacunza.biblio.system.dto.loan.LoanCreateRequest;
import com.lacunza.biblio.system.dto.loan.LoanResponse;
import com.lacunza.biblio.system.dto.loan.LoanUpdateRequest;
import com.lacunza.biblio.system.exception.ResourceNotFoundException;
import com.lacunza.biblio.system.model.Book;
import com.lacunza.biblio.system.model.Fine;
import com.lacunza.biblio.system.model.Loan;
import com.lacunza.biblio.system.model.Loan.LoanStatus;
import com.lacunza.biblio.system.model.User;
import com.lacunza.biblio.system.repository.BookRepository;
import com.lacunza.biblio.system.repository.FineRepository;
import com.lacunza.biblio.system.repository.LoanRepository;
import com.lacunza.biblio.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final FineRepository fineRepository;
    private final EmailService emailService;
    
    // Configuración de préstamos
    private static final int DEFAULT_LOAN_PERIOD_DAYS = 14;
    private static final int MAX_RENEWALS = 2;
    private static final int RENEWAL_PERIOD_DAYS = 7;
    private static final BigDecimal FINE_RATE_PER_DAY = new BigDecimal("1.00"); // $1 por día de retraso

    /**
     * Obtener todos los préstamos
     */
    @Transactional(readOnly = true)
    public Page<LoanResponse> getAllLoans(String status, Pageable pageable) {
        Page<Loan> loansPage;
        
        if (status != null && !status.isEmpty()) {
            try {
                LoanStatus loanStatus = LoanStatus.valueOf(status.toUpperCase());
                loansPage = loanRepository.findByStatus(loanStatus, pageable);
            } catch (IllegalArgumentException e) {
                // Si el estado no es válido, devolver todos los préstamos
                loansPage = loanRepository.findAll(pageable);
            }
        } else {
            loansPage = loanRepository.findAll(pageable);
        }
        
        return loansPage.map(this::convertToResponse);
    }

    /**
     * Obtener préstamos por usuario
     */
    @Transactional(readOnly = true)
    public Page<LoanResponse> getLoansByUser(Long userId, String status, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
        
        Page<Loan> loansPage;
        
        if (status != null && !status.isEmpty()) {
            try {
                LoanStatus loanStatus = LoanStatus.valueOf(status.toUpperCase());
                loansPage = loanRepository.findByUserAndStatus(user, loanStatus, pageable);
            } catch (IllegalArgumentException e) {
                // Si el estado no es válido, devolver todos los préstamos del usuario
                loansPage = loanRepository.findByUser(user, pageable);
            }
        } else {
            loansPage = loanRepository.findByUser(user, pageable);
        }
        
        return loansPage.map(this::convertToResponse);
    }

    /**
     * Obtener préstamos por libro
     */
    @Transactional(readOnly = true)
    public Page<LoanResponse> getLoansByBook(Long bookId, String status, Pageable pageable) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + bookId));
        
        Page<Loan> loansPage;
        
        if (status != null && !status.isEmpty()) {
            try {
                LoanStatus loanStatus = LoanStatus.valueOf(status.toUpperCase());
                List<Loan> loans = loanRepository.findByBookAndStatus(book, loanStatus);
                // Convertir lista a página
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), loans.size());
                List<Loan> pageContent = loans.subList(start, end);
                loansPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, loans.size());
            } catch (IllegalArgumentException e) {
                // Si el estado no es válido, devolver todos los préstamos del libro
                loansPage = loanRepository.findByBook(book, pageable);
            }
        } else {
            loansPage = loanRepository.findByBook(book, pageable);
        }
        
        return loansPage.map(this::convertToResponse);
    }

    /**
     * Obtener un préstamo por ID
     */
    @Transactional(readOnly = true)
    public LoanResponse getLoanById(Long id, User currentUser) {
        Loan loan = findLoanById(id);
        
        // Verificar permisos: solo el usuario del préstamo, bibliotecarios o administradores pueden ver los detalles
        if (!loan.getUser().getId().equals(currentUser.getId()) && 
                !currentUser.hasAnyRole("LIBRARIAN", "ADMIN")) {
            throw new AccessDeniedException("No tienes permiso para ver este préstamo");
        }
        
        return convertToResponse(loan);
    }

    /**
     * Crear un nuevo préstamo directo (mostrador, sin reserva previa).
     * Este método SÍ descuenta una copia disponible del libro, ya que no
     * proviene de una reserva que ya la haya descontado.
     */
    @Transactional
    public LoanResponse createLoan(LoanCreateRequest request, User librarian) {
        // Obtener el libro
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + request.getBookId()));

        // Verificar disponibilidad del libro
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No hay copias disponibles de este libro");
        }

        // Obtener el usuario
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + request.getUserId()));

        validateLoanEligibility(book, user);

        // Actualizar disponibilidad del libro (se descuenta aquí porque este
        // préstamo NO viene de una reserva ya procesada)
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Loan savedLoan = persistLoan(book, user, librarian);

        return convertToResponse(savedLoan);
    }

    /**
     * Crear un préstamo a partir de una reserva ya marcada como COMPLETED.
     * NO descuenta availableCopies porque ReservationService.completeReservation()
     * ya lo hizo al entregar físicamente el libro; este método solo se encarga
     * de generar el registro de Loan correspondiente para que quede un
     * seguimiento real (aparecer en "mis préstamos", poder devolverse, generar
     * multa por atraso, etc.).
     *
     * Antes de este fix, completar una reserva descontaba la copia y marcaba
     * la reserva como COMPLETED, pero nunca creaba el Loan: el libro
     * "desaparecía" del inventario sin que existiera ningún préstamo asociado.
     */
    @Transactional
    public LoanResponse createLoanFromReservation(Book book, User user, User librarian) {
        validateLoanEligibility(book, user);

        Loan savedLoan = persistLoan(book, user, librarian);

        return convertToResponse(savedLoan);
    }

    /**
     * Validaciones comunes antes de crear cualquier préstamo.
     */
    private void validateLoanEligibility(Book book, User user) {
        // Verificar si el usuario ya tiene un préstamo activo de este libro
        if (loanRepository.existsByBookAndUserAndStatus(book, user, LoanStatus.ACTIVE)) {
            throw new IllegalStateException("El usuario ya tiene un préstamo activo de este libro");
        }

        // Verificar si el usuario tiene multas pendientes
        List<Fine> unpaidFines = fineRepository.findByUserAndStatusNot(user, Fine.FineStatus.PAID);
        if (!unpaidFines.isEmpty()) {
            throw new IllegalStateException("El usuario tiene multas pendientes de pago");
        }
    }

    /**
     * Construye, guarda y notifica un Loan. No toca availableCopies: cada
     * llamador decide si corresponde descontar la copia o no.
     */
    private Loan persistLoan(Book book, User user, User librarian) {
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setUser(user);
        loan.setLibrarian(librarian);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(DEFAULT_LOAN_PERIOD_DAYS));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setRenewedCount(0);

        Loan savedLoan = loanRepository.save(loan);

        sendLoanConfirmationEmail(savedLoan);

        return savedLoan;
    }

    /**
     * Renovar un préstamo
     */
    @Transactional
    public LoanResponse renewLoan(Long id, User currentUser) {
        Loan loan = findLoanById(id);
        
        // Verificar permisos: solo el usuario del préstamo, bibliotecarios o administradores pueden renovar
        boolean isOwnLoan = loan.getUser().getId().equals(currentUser.getId());
        boolean isLibrarianOrAdmin = currentUser.hasAnyRole("LIBRARIAN", "ADMIN");
        
        if (!isOwnLoan && !isLibrarianOrAdmin) {
            throw new AccessDeniedException("No tienes permiso para renovar este préstamo");
        }
        
        // Verificar si el préstamo está activo
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new IllegalStateException("Solo se pueden renovar préstamos activos");
        }
        
        // Verificar si el préstamo está vencido
        if (loan.isOverdue()) {
            throw new IllegalStateException("No se puede renovar un préstamo vencido");
        }
        
        // Verificar si se ha alcanzado el límite de renovaciones
        if (loan.getRenewedCount() >= MAX_RENEWALS) {
            throw new IllegalStateException("Se ha alcanzado el límite de renovaciones para este préstamo");
        }
        
        // Renovar el préstamo
        loan.renew(RENEWAL_PERIOD_DAYS);
        Loan renewedLoan = loanRepository.save(loan);
        
        // Enviar correo de confirmación al usuario
        sendLoanRenewalEmail(renewedLoan);
        
        return convertToResponse(renewedLoan);
    }

    /**
     * Devolver un libro (marcar préstamo como devuelto)
     */
    @Transactional
    public LoanResponse returnBook(Long id, LoanUpdateRequest request, User librarian) {
        Loan loan = findLoanById(id);
        
        // Verificar si el préstamo está activo
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new IllegalStateException("Solo se pueden devolver préstamos activos");
        }
        
        // Verificar si el préstamo está vencido y crear multa si es necesario
        if (loan.isOverdue()) {
            int daysOverdue = (int) loan.calculateDaysOverdue();
            BigDecimal fineAmount = FINE_RATE_PER_DAY.multiply(new BigDecimal(daysOverdue));
            
            // Crear multa
            Fine fine = new Fine();
            fine.setLoan(loan);
            fine.setUser(loan.getUser());
            fine.setAmount(fineAmount);
            fine.setDaysOverdue(daysOverdue);
            fine.setStatus(Fine.FineStatus.PENDING);
            fine.setNotes("Multa por devolución tardía. Días de retraso: " + daysOverdue);
            
            fineRepository.save(fine);
            loan.setFine(fine);
            
            // Enviar notificación de multa al usuario
            sendFineNotificationEmail(fine);
        }
        
        // Marcar el préstamo como devuelto
        loan.returnBook(librarian);
        
        // Agregar notas si se proporcionan
        if (request != null && request.getNotes() != null) {
            loan.setNotes(request.getNotes());
        }
        
        // Actualizar disponibilidad del libro
        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);
        
        // Guardar el préstamo actualizado
        Loan returnedLoan = loanRepository.save(loan);
        
        // Enviar correo de confirmación al usuario
        sendReturnConfirmationEmail(returnedLoan);
        
        return convertToResponse(returnedLoan);
    }

    /**
     * Obtener préstamos vencidos
     */
    @Transactional(readOnly = true)
    public Page<LoanResponse> getOverdueLoans(Pageable pageable) {
        List<Loan> overdueLoans = loanRepository.findByDueDateBeforeAndStatus(
                LocalDate.now(), LoanStatus.ACTIVE);
        
        // Convertir lista a página
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), overdueLoans.size());
        List<Loan> pageContent = overdueLoans.subList(start, end);
        Page<Loan> loansPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, overdueLoans.size());
        
        return loansPage.map(this::convertToResponse);
    }

    /**
     * Obtener préstamos que vencen pronto
     */
    @Transactional(readOnly = true)
    public Page<LoanResponse> getLoansExpiringSoon(int days, Pageable pageable) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        
        List<Loan> expiringLoans = loanRepository.findLoansExpiringBetween(
                today, endDate, LoanStatus.ACTIVE);
        
        // Convertir lista a página
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), expiringLoans.size());
        List<Loan> pageContent = expiringLoans.subList(start, end);
        Page<Loan> loansPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, expiringLoans.size());
        
        return loansPage.map(this::convertToResponse);
    }

    /**
     * Obtener estadísticas de préstamos por período
     */
    public List<Object[]> getLoanStatsByPeriod(LocalDate startDate, LocalDate endDate) {
        return loanRepository.getLoanStatsByMonth(startDate, endDate);
    }

    /**
     * Tarea programada para verificar préstamos vencidos y enviar notificaciones
     */
    @Scheduled(cron = "0 0 8 * * ?") // Ejecutar todos los días a las 8:00 AM
    @Transactional
    public void checkOverdueLoans() {
        LocalDate today = LocalDate.now();
        
        // Buscar préstamos vencidos
        List<Loan> overdueLoans = loanRepository.findByDueDateBeforeAndStatus(
                today, LoanStatus.ACTIVE);
        
        for (Loan loan : overdueLoans) {
            // Enviar recordatorio al usuario
            sendOverdueNotificationEmail(loan);
        }
        
        // Buscar préstamos que vencen en los próximos 3 días
        LocalDate threeDaysLater = today.plusDays(3);
        List<Loan> expiringLoans = loanRepository.findLoansExpiringBetween(
                today, threeDaysLater, LoanStatus.ACTIVE);
        
        for (Loan loan : expiringLoans) {
            // Enviar recordatorio al usuario
            sendExpiringLoanNotificationEmail(loan);
        }
    }

    /**
     * Método auxiliar para buscar un préstamo por ID
     */
    private Loan findLoanById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado con ID: " + id));
    }

    /**
     * Método auxiliar para convertir un Loan a LoanResponse
     */
    private LoanResponse convertToResponse(Loan loan) {
        LoanResponse response = new LoanResponse();
        response.setId(loan.getId());
        response.setBookId(loan.getBook().getId());
        response.setBookTitle(loan.getBook().getTitle());
        response.setUserId(loan.getUser().getId());
        response.setUserName(loan.getUser().getFirstName() + " " + loan.getUser().getLastName());
        response.setLoanDate(loan.getLoanDate());
        response.setDueDate(loan.getDueDate());
        response.setReturnDate(loan.getReturnDate());
        response.setStatus(loan.getStatus().name());
        response.setRenewedCount(loan.getRenewedCount());
        response.setNotes(loan.getNotes());
        
        // Agregar información del bibliotecario si existe
        if (loan.getLibrarian() != null) {
            response.setLibrarianId(loan.getLibrarian().getId());
            response.setLibrarianName(loan.getLibrarian().getFirstName() + " " + loan.getUser().getLastName());
        }
        
        // Agregar información de multa si existe
        if (loan.getFine() != null) {
            response.setFineId(loan.getFine().getId());
            response.setFineAmount(loan.getFine().getAmount());
            response.setFineStatus(loan.getFine().getStatus().name());
        }
        
        // Calcular días restantes o días de retraso
        if (loan.getStatus() == LoanStatus.ACTIVE) {
            long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), loan.getDueDate());
            if (daysRemaining < 0) {
                response.setDaysOverdue(Math.abs((int) daysRemaining));
                response.setOverdue(true);
                // En BD el préstamo sigue ACTIVE hasta que se devuelve (ahí se genera la multa);
                // hacia el cliente se informa como OVERDUE para que se muestre "Vencido".
                response.setStatus(LoanStatus.OVERDUE.name());
            } else {
                response.setDaysRemaining((int) daysRemaining);
                response.setOverdue(false);
            }
        }
        
        return response;
    }

    /**
     * Métodos para enviar correos electrónicos
     */
    private void sendLoanConfirmationEmail(Loan loan) {
        String subject = "Confirmación de préstamo - Biblioteca";
        String content = String.format(
                "Estimado/a %s,\n\n" +
                "Le confirmamos que ha tomado prestado el libro '%s'.\n" +
                "Fecha de préstamo: %s\n" +
                "Fecha de devolución: %s\n\n" +
                "Por favor, devuelva el libro antes de la fecha indicada para evitar multas.\n\n" +
                "Saludos cordiales,\n" +
                "El equipo de la Biblioteca",
                loan.getUser().getFirstName() + " " + loan.getUser().getLastName(),
                loan.getBook().getTitle(),
                loan.getLoanDate(),
                loan.getDueDate());
        
        emailService.sendEmail(loan.getUser().getEmail(), subject, content);
    }

    private void sendLoanRenewalEmail(Loan loan) {
        String subject = "Renovación de préstamo - Biblioteca";
        String content = String.format(
                "Estimado/a %s,\n\n" +
                "Le confirmamos que su préstamo del libro '%s' ha sido renovado.\n" +
                "Nueva fecha de devolución: %s\n" +
                "Número de renovaciones realizadas: %d/%d\n\n" +
                "Por favor, devuelva el libro antes de la fecha indicada para evitar multas.\n\n" +
                "Saludos cordiales,\n" +
                "El equipo de la Biblioteca",
                loan.getUser().getFirstName() + " " + loan.getUser().getLastName(),
                loan.getBook().getTitle(),
                loan.getDueDate(),
                loan.getRenewedCount(),
                MAX_RENEWALS);
        
        emailService.sendEmail(loan.getUser().getEmail(), subject, content);
    }

    private void sendReturnConfirmationEmail(Loan loan) {
        String subject = "Confirmación de devolución - Biblioteca";
        String content = String.format(
                "Estimado/a %s,\n\n" +
                "Le confirmamos que ha devuelto el libro '%s'.\n" +
                "Fecha de devolución: %s\n\n" +
                "Gracias por utilizar nuestros servicios.\n\n" +
                "Saludos cordiales,\n" +
                "El equipo de la Biblioteca",
                loan.getUser().getFirstName() + " " + loan.getUser().getLastName(),
                loan.getBook().getTitle(),
                loan.getReturnDate());
        
        emailService.sendEmail(loan.getUser().getEmail(), subject, content);
    }

    private void sendOverdueNotificationEmail(Loan loan) {
        int daysOverdue = (int) loan.calculateDaysOverdue();
        BigDecimal estimatedFine = FINE_RATE_PER_DAY.multiply(new BigDecimal(daysOverdue));
        
        String subject = "Préstamo vencido - Biblioteca";
        String content = String.format(
                "Estimado/a %s,\n\n" +
                "Le recordamos que el préstamo del libro '%s' está vencido.\n" +
                "Fecha de vencimiento: %s\n" +
                "Días de retraso: %d\n" +
                "Multa estimada: $%s\n\n" +
                "Por favor, devuelva el libro lo antes posible para evitar que la multa siga aumentando.\n\n" +
                "Saludos cordiales,\n" +
                "El equipo de la Biblioteca",
                loan.getUser().getFirstName() + " " + loan.getUser().getLastName(),
                loan.getBook().getTitle(),
                loan.getDueDate(),
                daysOverdue,
                estimatedFine.toString());
        
        emailService.sendEmail(loan.getUser().getEmail(), subject, content);
    }

    private void sendExpiringLoanNotificationEmail(Loan loan) {
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), loan.getDueDate());
        
        String subject = "Recordatorio de devolución - Biblioteca";
        String content = String.format(
                "Estimado/a %s,\n\n" +
                "Le recordamos que el préstamo del libro '%s' vence pronto.\n" +
                "Fecha de vencimiento: %s\n" +
                "Días restantes: %d\n\n" +
                "Por favor, devuelva el libro antes de la fecha indicada para evitar multas, o renuévelo si es posible.\n\n" +
                "Saludos cordiales,\n" +
                "El equipo de la Biblioteca",
                loan.getUser().getFirstName() + " " + loan.getUser().getLastName(),
                loan.getBook().getTitle(),
                loan.getDueDate(),
                daysRemaining);
        
        emailService.sendEmail(loan.getUser().getEmail(), subject, content);
    }

    private void sendFineNotificationEmail(Fine fine) {
        String subject = "Notificación de multa - Biblioteca";
        String content = String.format(
                "Estimado/a %s,\n\n" +
                "Le informamos que se ha generado una multa por la devolución tardía del libro '%s'.\n" +
                "Días de retraso: %d\n" +
                "Monto de la multa: $%s\n\n" +
                "Por favor, realice el pago lo antes posible.\n\n" +
                "Saludos cordiales,\n" +
                "El equipo de la Biblioteca",
                fine.getUser().getFirstName() + " " + fine.getUser().getLastName(),
                fine.getLoan().getBook().getTitle(),
                fine.getDaysOverdue(),
                fine.getAmount().toString());
        
        emailService.sendEmail(fine.getUser().getEmail(), subject, content);
    }
}