package com.lacunza.biblio.system.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 255)
    private String description;

    // Roles predefinidos para el sistema.
    // Nota: el nombre incluye el prefijo "ROLE_" porque así se almacena en la
    // tabla `roles` (ver schema.sql). Spring Security requiere ese prefijo
    // para que hasRole()/hasAuthority() funcionen correctamente.
    public static final String ADMIN = "ROLE_ADMIN";
    public static final String LIBRARIAN = "ROLE_LIBRARIAN";
    public static final String MEMBER = "ROLE_MEMBER";
    public static final String VISITOR = "ROLE_VISITOR";
}