package pe.com.lacunza.book.library.dto.loan;

public record LoanRequest(
        Long bookId,
        Long userId, // Opcional si el usuario se presta a sí mismo, obligatorio si lo hace un bibliotecario
        Integer days // Cuántos días se lo lleva (opcional, default 7 o 15)
) {}
