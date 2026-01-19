package pe.com.lacunza.book.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.com.lacunza.book.library.dto.ErrorResponse;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Manejo de Credenciales Inválidas (Login fallido)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", "El correo o la contraseña son incorrectos.");
    }

    // 2. Manejo de Usuario Deshabilitado (No verificado)
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledUser(DisabledException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Cuenta deshabilitada", "Debes verificar tu correo electrónico antes de iniciar sesión.");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameException(UsernameNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Usuario no encontrado!", "El correo de este usuario no esta registrado.");
    }

    // 3. Manejo de Errores Genéricos (Tus RuntimeException del AuthService)
    // Lo ideal sería crear tus propias excepciones (ej: EmailAlreadyExistsException),
    // pero esto capturará los "throw new RuntimeException(...)" que pusimos.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        // Si el mensaje es sobre duplicados, devolvemos 409 Conflict
        if (ex.getMessage().contains("ya está registrado")) {
            return buildResponse(HttpStatus.CONFLICT, "Conflicto de datos", ex.getMessage());
        }
        // Para otros errores de lógica (código expirado, incorrecto, etc), devolvemos 400 Bad Request
        return buildResponse(HttpStatus.BAD_REQUEST, "Error en la solicitud", ex.getMessage());
    }

    // 4. Manejo General (Cualquier otra cosa que se nos escape)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ha ocurrido un error inesperado. Contacte al soporte.");
    }

    // Método auxiliar para construir la respuesta
    private ResponseEntity<pe.com.lacunza.book.library.dto.ErrorResponse> buildResponse(HttpStatus status, String error, String message) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message
        );
        return new ResponseEntity<>(response, status);
    }
}
