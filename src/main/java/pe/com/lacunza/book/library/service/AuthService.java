package pe.com.lacunza.book.library.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pe.com.lacunza.book.library.dto.auth.AuthResponse;
import pe.com.lacunza.book.library.dto.auth.LoginRequest;
import pe.com.lacunza.book.library.dto.auth.RegisterRequest;
import pe.com.lacunza.book.library.dto.auth.VerifyRequest;
import pe.com.lacunza.book.library.model.Role;
import pe.com.lacunza.book.library.model.User;
import pe.com.lacunza.book.library.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    public String register(RegisterRequest request) {
        // 1. Buscar si el usuario ya existe
        var existingUserOpt = userRepository.findByEmail(request.email());

        User user;

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // CASO A: El usuario existe y YA está verificado -> Error
            if (existingUser.isEnabled()) {
                throw new RuntimeException("El email ya está registrado y verificado.");
            }

            // CASO B: El usuario existe pero NO está verificado -> Actualizamos datos y reenvíamos código
            user = existingUser;
            user.setFirstname(request.firstname());
            user.setLastname(request.lastname());
            // Actualizamos la contraseña por si se le olvidó la que puso en el primer intento fallido
            user.setPassword(passwordEncoder.encode(request.password()));

        } else {
            // CASO C: Usuario nuevo -> Creamos desde cero
            user = User.builder()
                    .firstname(request.firstname())
                    .lastname(request.lastname())
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .role(Role.MEMBER)
                    .enabled(false)
                    .build();
        }

        // 2. Generar NUEVO código (común para Caso B y C)
        String code = generateVerificationCode();
        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));

        // 3. Guardar (Update o Insert según sea el caso)
        userRepository.save(user);

        // 4. Enviar email
        try {
            emailService.sendVerificationCode(request.email(), code);
        } catch (MessagingException e) {
            // Opcional: Si falla el correo, podrías querer hacer rollback,
            // pero por ahora lanzar la excepción está bien.
            throw new RuntimeException("Error al enviar el email de verificación");
        }

        return "Se ha enviado un código de verificación a tu correo.";
    }

    public AuthResponse verify(VerifyRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.isEnabled()) {
            throw new RuntimeException("La cuenta ya está verificada");
        }

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El código ha expirado");
        }

        if (!user.getVerificationCode().equals(request.code())) {
            throw new RuntimeException("Código incorrecto");
        }

        // Activar usuario
        user.setEnabled(true);
        user.setVerificationCode(null); // Limpiar código
        userRepository.save(user);

        // Generar Token
        var jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

    public AuthResponse login(LoginRequest request) {
        // Esto autentica al usuario. Si falla, lanza una excepción automáticamente.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // Si llegamos aquí, el usuario es válido. Buscamos sus datos para generar el token.
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con email: " + request.email()
                ));

        var jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // Genera número entre 100000 y 999999
        return String.valueOf(code);
    }
}
