package com.lacunza.biblio.system.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private boolean enabled;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Estadísticas del usuario
    private Integer activeLoansCount;
    private Integer pendingFinesCount;
    private Integer activeReservationsCount;
}