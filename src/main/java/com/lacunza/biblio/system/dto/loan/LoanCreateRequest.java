package com.lacunza.biblio.system.dto.loan;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanCreateRequest {
    
    @NotNull(message = "El ID del libro es obligatorio")
    private Long bookId;
    
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long userId;
}