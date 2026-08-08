package com.lacunza.biblio.system.dto.book;

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
public class BookDTO {
    
    private Long id;
    private String title;
    private String isbn;
    private Integer publicationYear;
    private String edition;
    private String publisher;
    private String language;
    private String physicalLocation;
    private Integer totalCopies;
    private Integer availableCopies;
    private String description;
    private String coverImageUrl;
    
    private Set<AuthorDTO> authors;
    private Set<CategoryDTO> categories;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Información adicional para la UI
    private boolean available;
    private boolean reserved;
    private Long reservationCount;
    private Long loanCount;
}