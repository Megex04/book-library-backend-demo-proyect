package com.lacunza.biblio.system.controller;

import com.lacunza.biblio.system.dto.book.AuthorDTO;
import com.lacunza.biblio.system.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de solo lectura para autores. Reintroducido para poder listar
 * autores desde el frontend (combo box al editar un libro). No expone
 * create/update/delete: la gestión de autores no tiene todavía una pantalla
 * dedicada, así que no hay consumidor real para esas operaciones.
 */
@RestController
@RequestMapping("/api/public/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    /**
     * Obtener todos los autores (paginados)
     * Accesible para todos los usuarios
     */
    @GetMapping
    public ResponseEntity<Page<AuthorDTO>> getAllAuthors(
            @PageableDefault(size = 100, sort = "name") Pageable pageable) {

        Page<AuthorDTO> authors = authorService.getAllAuthors(pageable);
        return ResponseEntity.ok(authors);
    }

    /**
     * Buscar autores por nombre
     * Accesible para todos los usuarios
     */
    @GetMapping("/search")
    public ResponseEntity<Page<AuthorDTO>> searchAuthors(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        Page<AuthorDTO> authors = authorService.searchAuthorsByName(name, pageable);
        return ResponseEntity.ok(authors);
    }
}
