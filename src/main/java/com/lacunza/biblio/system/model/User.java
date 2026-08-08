package com.lacunza.biblio.system.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// Ver comentario equivalente en Book.java: sin esto, @Data genera hashCode/equals
// sobre la colección lazy "roles", causando cargas lazy en cascada y
// ConcurrentModificationException bajo Hibernate.
@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean accountNonExpired;

    @Column(nullable = false)
    private boolean accountNonLocked;

    @Column(nullable = false)
    private boolean credentialsNonExpired;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expiry")
    private LocalDateTime resetPasswordTokenExpiry;

    @Column(name = "refresh_token")
    private String refreshToken;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Verifica si el usuario tiene alguno de los roles especificados.
     *
     * Acepta tanto el nombre "corto" (ej. "ADMIN") como el nombre completo
     * almacenado en BD (ej. "ROLE_ADMIN"). Los roles reales en la tabla
     * `roles` llevan el prefijo ROLE_ (ver Role.java), pero varios llamadores
     * (LoanService, FineService) invocaban este método con el nombre corto
     * asumiendo que Spring Security anteponía el prefijo automáticamente
     * -cosa que sí hace @PreAuthorize("hasAnyRole(...)"), pero este método
     * casero no lo hacía, comparando de forma exacta y devolviendo siempre
     * false para un ADMIN real. Esto bloqueaba renovar/ver préstamos y multas
     * incluso para administradores.
     *
     * @param roleNames nombres de los roles a verificar
     * @return true si el usuario tiene al menos uno de los roles especificados
     */
    public boolean hasAnyRole(String... roleNames) {
        for (String roleName : roleNames) {
            String normalized = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
            if (roles.stream().anyMatch(role -> role.getName().equals(roleName) || role.getName().equals(normalized))) {
                return true;
            }
        }
        return false;
    }
}