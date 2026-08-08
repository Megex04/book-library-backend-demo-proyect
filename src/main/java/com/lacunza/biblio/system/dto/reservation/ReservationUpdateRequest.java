package com.lacunza.biblio.system.dto.reservation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationUpdateRequest {
    
    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 255, message = "El motivo no puede exceder los 255 caracteres")
    private String reason;
}