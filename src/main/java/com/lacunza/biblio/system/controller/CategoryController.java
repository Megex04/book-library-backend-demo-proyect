package com.lacunza.biblio.system.controller;

import com.lacunza.biblio.system.dto.book.BookDTO;
import com.lacunza.biblio.system.dto.book.CategoryDTO;
import com.lacunza.biblio.system.model.User;
import com.lacunza.biblio.system.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Obtener todas las categorías (paginadas)
     * Accesible para todos los usuarios
     */
    @GetMapping("/public/categories")
    public ResponseEntity<Page<CategoryDTO>> getAllCategories(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        
        Page<CategoryDTO> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }

    /**
     * Obtener todas las categorías principales (sin categoría padre)
     * Accesible para todos los usuarios
     */
    @GetMapping("/public/categories/main")
    public ResponseEntity<List<CategoryDTO>> getMainCategories() {
        List<CategoryDTO> mainCategories = categoryService.getMainCategories();
        return ResponseEntity.ok(mainCategories);
    }

    /**
     * Buscar categorías por nombre
     * Accesible para todos los usuarios
     */
    @GetMapping("/public/categories/search")
    public ResponseEntity<Page<CategoryDTO>> searchCategories(
            @RequestParam String name,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        
        Page<CategoryDTO> categories = categoryService.searchCategoriesByName(name, pageable);
        return ResponseEntity.ok(categories);
    }

    /**
     * Obtener una categoría por ID
     * Accesible para todos los usuarios
     */
    @GetMapping("/public/categories/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Obtener subcategorías de una categoría
     * Accesible para todos los usuarios
     */
    @GetMapping("/public/categories/{id}/subcategories")
    public ResponseEntity<List<CategoryDTO>> getSubcategories(@PathVariable Long id) {
        List<CategoryDTO> subcategories = categoryService.getSubcategories(id);
        return ResponseEntity.ok(subcategories);
    }

    /**
     * Obtener libros de una categoría
     * Accesible para todos los usuarios
     */
    @GetMapping("/public/categories/{id}/books")
    public ResponseEntity<Page<BookDTO>> getCategoryBooks(
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        
        Page<BookDTO> books = categoryService.getCategoryBooks(id, pageable);
        return ResponseEntity.ok(books);
    }

    /**
     * Crear una nueva categoría
     * Solo para bibliotecarios y administradores
     */
    @PostMapping("/admin/categories")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<CategoryDTO> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO,
            @AuthenticationPrincipal User currentUser) {
        
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    /**
     * Actualizar una categoría existente
     * Solo para bibliotecarios y administradores
     */
    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO,
            @AuthenticationPrincipal User currentUser) {
        
        CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * Eliminar una categoría
     * Solo para administradores
     */
    @DeleteMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener categorías más populares (con más libros)
     * Accesible para todos los usuarios
     */
    @GetMapping("/public/categories/most-popular")
    public ResponseEntity<Page<CategoryDTO>> getMostPopularCategories(
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<CategoryDTO> categories = categoryService.getMostPopularCategories(pageable);
        return ResponseEntity.ok(categories);
    }
}