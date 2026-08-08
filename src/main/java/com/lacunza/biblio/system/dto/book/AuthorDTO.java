package com.lacunza.biblio.system.dto.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDTO {
    
    private Long id;
    private String name;
    private String biography;
    private Integer birthYear;
    private Integer deathYear;
    private String nationality;
}