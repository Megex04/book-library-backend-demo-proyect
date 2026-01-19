package pe.com.lacunza.book.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.lacunza.book.library.dto.loan.LoanRequest;
import pe.com.lacunza.book.library.dto.loan.LoanResponse;
import pe.com.lacunza.book.library.model.Book;
import pe.com.lacunza.book.library.model.Loan;
import pe.com.lacunza.book.library.model.LoanStatus;
import pe.com.lacunza.book.library.model.User;
import pe.com.lacunza.book.library.repository.BookRepository;
import pe.com.lacunza.book.library.repository.LoanRepository;
import pe.com.lacunza.book.library.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    // --- CREAR PRÉSTAMO ---
    @Transactional
    public LoanResponse createLoan(LoanRequest request) {
        // 1. Buscar Usuario
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Buscar Libro
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        // 3. Validaciones de Negocio
        if (book.getStock() <= 0) {
            throw new RuntimeException("No hay stock disponible para este libro");
        }

        if (loanRepository.hasOverdueLoans(user.getId())) {
            throw new RuntimeException("El usuario tiene préstamos vencidos pendientes");
        }

        // Opcional: Límite de 3 libros activos
        List<Loan> activeLoans = loanRepository.findByUserIdAndStatus(user.getId(), LoanStatus.ACTIVE);
        if (activeLoans.size() >= 3) {
            throw new RuntimeException("El usuario ya tiene el máximo de libros prestados (3)");
        }

        // 4. Crear Préstamo
        int days = (request.days() != null && request.days() > 0) ? request.days() : 15; // Default 15 días

        Loan loan = Loan.builder()
                .user(user)
                .book(book)
                .loanDate(LocalDate.now())
                .returnDate(LocalDate.now().plusDays(days))
                .status(LoanStatus.ACTIVE)
                .build();

        // 5. Actualizar Stock del Libro
        book.setStock(book.getStock() - 1);
        bookRepository.save(book); // El @PreUpdate actualizará 'available'

        Loan savedLoan = loanRepository.save(loan);
        return mapToResponse(savedLoan);
    }

    // --- DEVOLVER PRÉSTAMO ---
    @Transactional
    public LoanResponse returnLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new RuntimeException("Este préstamo ya fue devuelto o procesado");
        }

        // 1. Actualizar estado
        loan.setActualReturnDate(LocalDate.now());

        if (LocalDate.now().isAfter(loan.getReturnDate())) {
            loan.setStatus(LoanStatus.OVERDUE); // Se marca como vencido aunque lo devuelva (para historial)
            // Aquí podrías generar una multa (Fine)
        } else {
            loan.setStatus(LoanStatus.RETURNED);
        }

        // 2. Devolver Stock al Libro
        Book book = loan.getBook();
        book.setStock(book.getStock() + 1);
        bookRepository.save(book);

        Loan savedLoan = loanRepository.save(loan);
        return mapToResponse(savedLoan);
    }

    // --- CONSULTAS ---
    public List<LoanResponse> getLoansByUserId(Long userId) {
        return loanRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<LoanResponse> getAllLoans() {
        return loanRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    private LoanResponse mapToResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getBook().getId(),
                loan.getBook().getTitle(),
                loan.getUser().getId(),
                loan.getUser().getEmail(),
                loan.getLoanDate(),
                loan.getReturnDate(),
                loan.getActualReturnDate(),
                loan.getStatus()
        );
    }
}
