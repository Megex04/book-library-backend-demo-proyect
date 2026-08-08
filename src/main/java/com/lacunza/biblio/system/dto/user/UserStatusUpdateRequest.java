package com.lacunza.biblio.system.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusUpdateRequest {
    
    @NotNull(message = "El estado es obligatorio")
    private Boolean enabled;
    
    private String reason;
}