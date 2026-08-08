package com.lacunza.biblio.system.service;

import com.lacunza.biblio.system.dto.auth.LoginRequest;
import com.lacunza.biblio.system.dto.auth.RegisterRequest;
import com.lacunza.biblio.system.dto.auth.TokenResponse;
import com.lacunza.biblio.system.exception.ResourceNotFoundException;
import com.lacunza.biblio.system.exception.TokenException;
import com.lacunza.biblio.system.exception.UserAlreadyExistsException;
import com.lacunza.biblio.system.model.Role;
import com.lacunza.biblio.system.model.User;
import com.lacunza.biblio.system.repository.RoleRepository;
import com.lacunza.biblio.system.repository.UserRepository;
import com.lacunza.biblio.system.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("El email ya está registrado");
        }

        Role memberRole = roleRepository.findByName(Role.MEMBER)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", Role.MEMBER));

        String verificationToken = UUID.randomUUID().toString();
        LocalDateTime tokenExpiry = LocalDateTime.now().plusDays(1);

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .roles(Collections.singleton(memberRole))
                .enabled(false)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .verificationToken(verificationToken)
                .verificationTokenExpiry(tokenExpiry)
                .build();

        userRepository.save(user);

        // Enviar email de verificación
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.createToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken(request.getEmail());

        // Guardar refresh token en la base de datos
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", request.getEmail()));
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public void verifyAccount(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new TokenException("Token de verificación inválido"));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new TokenException("Token de verificación expirado");
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));

        String resetToken = UUID.randomUUID().toString();
        LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(1);

        user.setResetPasswordToken(resetToken);
        user.setResetPasswordTokenExpiry(tokenExpiry);
        userRepository.save(user);

        // Enviar email con token de recuperación
        emailService.sendPasswordResetEmail(email, resetToken);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new TokenException("Token de recuperación inválido"));

        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new TokenException("Token de recuperación expirado");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new TokenException("Refresh token inválido");
        }

        String username = tokenProvider.extractUsername(refreshToken);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", username));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new TokenException("Refresh token no coincide");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());

        String newAccessToken = tokenProvider.createToken(authentication);
        String newRefreshToken = tokenProvider.createRefreshToken(username);

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new TokenException("Refresh token inválido"));

        user.setRefreshToken(null);
        userRepository.save(user);
    }
}