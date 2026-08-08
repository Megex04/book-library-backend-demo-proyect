package com.lacunza.biblio.system.dto.reservation;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreateRequest {
    
    @NotNull(message = "El ID del libro es obligatorio")
    private Long bookId;
}