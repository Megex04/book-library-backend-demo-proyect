package com.lacunza.biblio.system.dto.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    
    private Long id;
    private String name;
    private String description;
    
    private Long parentId;
    private String parentName;
    
    private Set<CategoryDTO> subcategories;
}