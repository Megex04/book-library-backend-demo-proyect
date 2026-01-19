package pe.com.lacunza.book.library.model;

public enum LoanStatus {
    ACTIVE,     // Libro prestado, aún no devuelto
    RETURNED,   // Libro devuelto correctamente
    OVERDUE     // Vencido (se marca automáticamente o al intentar devolver tarde)
}
