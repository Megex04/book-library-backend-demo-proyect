package pe.com.lacunza.book.library.dto.auth;

public record VerifyRequest(
        String email,
        String code) {}
