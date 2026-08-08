package com.lacunza.biblio.system.controller;

import com.lacunza.biblio.system.dto.book.AuthorDTO;
import com.lacunza.biblio.system.dto.book.BookDTO;
import com.lacunza.biblio.system.model.User;
import com.lacunza.biblio.system.service.AuthorService;
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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    /**
     * Obtener todos los autores (paginados)
     * Accesible para todos los usuarios
     */
    @GetMapping("/public/authors")
    public ResponseEntity<Page<AuthorDTO>> getAllAuthors(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        
        Page<AuthorDTO> authors = authorService.getAllAuthors(pageable);
        return ResponseEntity.ok(authors);
    }

    /**
     * Buscar autores por nombre
     * Accesible para todos los usuarios
     */
    @GetMapping("/public/authors/search")
    public ResponseEntity<Page<AuthorDTO>> searchAuthors(
            @RequestParam String name,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        
        Page<AuthorDTO> authors = authorService.searchAuthorsByName(name, pageable);
        return ResponseEntity.ok(authors);
    }

    /**
     * Obtener un autor por ID
     * Accesible para todos los usuarios
     */
    @GetMapping("/public/authors/{id}")
    public ResponseEntity<AuthorDTO> getAuthorById(@PathVariable Long id) {
        AuthorDTO author = authorService.getAuthorById(id);
        return ResponseEntity.ok(author);
    }

    /**
     * Obtener libros de un autor
     * Accesible para todos los usuarios
     */
    @GetMapping("/public/authors/{id}/books")
    public ResponseEntity<Page<BookDTO>> getAuthorBooks(
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        
        Page<BookDTO> books = authorService.getAuthorBooks(id, pageable);
        return ResponseEntity.ok(books);
    }

    /**
     * Crear un nuevo autor
     * Solo para bibliotecarios y administradores
     */
    @PostMapping("/admin/authors")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<AuthorDTO> createAuthor(
            @Valid @RequestBody AuthorDTO authorDTO,
            @AuthenticationPrincipal User currentUser) {
        
        AuthorDTO createdAuthor = authorService.createAuthor(authorDTO);
        return new ResponseEntity<>(createdAuthor, HttpStatus.CREATED);
    }

    /**
     * Actualizar un autor existente
     * Solo para bibliotecarios y administradores
     */
    @PutMapping("/admin/authors/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<AuthorDTO> updateAuthor(
            @PathVariable Long id,
            @Valid @RequestBody AuthorDTO authorDTO,
            @AuthenticationPrincipal User currentUser) {
        
        AuthorDTO updatedAuthor = authorService.updateAuthor(id, authorDTO);
        return ResponseEntity.ok(updatedAuthor);
    }

    /**
     * Eliminar un autor
     * Solo para administradores
     */
    @DeleteMapping("/admin/authors/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener autores más prolíficos
     * Accesible para todos los usuarios
     */
    @GetMapping("/public/authors/most-prolific")
    public ResponseEntity<Page<AuthorDTO>> getMostProlificAuthors(
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<AuthorDTO> authors = authorService.getMostProlificAuthors(pageable);
        return ResponseEntity.ok(authors);
    }
}