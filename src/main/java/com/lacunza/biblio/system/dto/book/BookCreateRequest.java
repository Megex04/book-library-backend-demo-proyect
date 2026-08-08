package com.lacunza.biblio.system.dto.book;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCreateRequest {
    
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título no puede exceder los 255 caracteres")
    private String title;
    
    @Pattern(regexp = "^(?:ISBN(?:-13)?:?\\s)?(?=[0-9]{13}$|(?=(?:[0-9]+[-\\s]){4})[-\\s0-9]{17}$)97[89][-\\s]?[0-9]{1,5}[-\\s]?[0-9]+[-\\s]?[0-9]+[-\\s]?[0-9]$",
             message = "Formato de ISBN inválido")
    private String isbn;
    
    @Min(value = 1000, message = "El año de publicación debe ser mayor a 1000")
    private Integer publicationYear;
    
    private String edition;
    
    private String publisher;
    
    private String language;
    
    private String physicalLocation;
    
    @Min(value = 0, message = "El número de copias totales no puede ser negativo")
    private Integer totalCopies;
    
    @Min(value = 0, message = "El número de copias disponibles no puede ser negativo")
    private Integer availableCopies;
    
    private String description;
    
    private String coverImageUrl;
    
    private Set<Long> authorIds;
    
    private Set<Long> categoryIds;
}