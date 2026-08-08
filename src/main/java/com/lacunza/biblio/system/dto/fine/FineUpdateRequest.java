package com.lacunza.biblio.system.dto.fine;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FineUpdateRequest {
    
    @Size(max = 500, message = "Las notas no pueden exceder los 500 caracteres")
    private String notes;
}