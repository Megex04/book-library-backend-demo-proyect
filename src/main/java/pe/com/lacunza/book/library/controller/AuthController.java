package pe.com.lacunza.book.library.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.lacunza.book.library.dto.auth.AuthResponse;
import pe.com.lacunza.book.library.dto.auth.LoginRequest;
import pe.com.lacunza.book.library.dto.auth.RegisterRequest;
import pe.com.lacunza.book.library.dto.auth.VerifyRequest;
import pe.com.lacunza.book.library.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verify(@RequestBody VerifyRequest request) {
        return ResponseEntity.ok(authService.verify(request));
    }
}
