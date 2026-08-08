package com.lacunza.biblio.system.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleUpdateRequest {
    
    @NotEmpty(message = "Debe especificar al menos un rol")
    private Set<String> roles;
}