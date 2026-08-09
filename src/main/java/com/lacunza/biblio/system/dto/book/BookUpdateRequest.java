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
public class BookUpdateRequest {
    
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título no puede exceder los 255 caracteres")
    private String title;
    
    // Acepta ISBN-13 (empieza en 978/979) con o sin separadores (guion o
    // espacio), sin exigir una cantidad fija de grupos. La regex anterior
    // exigía exactamente 4 separadores intermedios (formato tipo
    // 978-3-16-148410-0), por lo que rechazaba ISBNs igualmente válidos como
    // 978-8408175774 (un solo guion tras el prefijo) o el mismo número sin
    // ningún separador.
    @Pattern(regexp = "^(?:ISBN(?:-13)?:?\\s)?97[89][-\\s]?[0-9]{1,5}[-\\s]?[0-9]{1,7}[-\\s]?[0-9]{1,7}[-\\s]?[0-9]$",
             message = "Formato de ISBN inválido")
    private String isbn;
    
    @Min(value = 1000, message = "El año de publicación debe ser mayor a 1000")
    private Integer publicationYear;
    
    private String edition;
    
    private String publisher;
    
    private String language;
    
    private String physicalLocation;
    
    private String description;
    
    private String coverImageUrl;
    
    private Set<Long> authorIds;
    
    private Set<Long> categoryIds;
}