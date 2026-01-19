package pe.com.lacunza.book.library.dto.auth;

public record RegisterRequest(
        String firstname,
        String lastname,
        String email,
        String password
) {}
