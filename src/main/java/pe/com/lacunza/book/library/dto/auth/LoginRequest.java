package pe.com.lacunza.book.library.dto.auth;

public record LoginRequest(
        String email,
        String password
) {}
