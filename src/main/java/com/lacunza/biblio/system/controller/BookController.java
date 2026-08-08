package com.lacunza.biblio.system.controller;

import com.lacunza.biblio.system.dto.book.BookDTO;
import com.lacunza.biblio.system.dto.book.BookCreateRequest;
import com.lacunza.biblio.system.dto.book.BookUpdateRequest;
import com.lacunza.biblio.system.model.User;
import com.lacunza.biblio.system.service.BookService;
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
public class BookController {

    private final BookService bookService;

    /**
     * Obtener todos los libros (paginados)
     * Accesible para todos los usuarios
     */
    @GetMapping("/public/books")
    public ResponseEntity<Page<BookDTO>> getAllBooks(
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        
        Page<BookDTO> books = bookService.getAllBooks(pageable);
        return ResponseEntity.ok(books);
    }

    /**
     * Buscar libros por título, autor, categoría, etc.
     * Accesible para todos los usuarios
     */
    @GetMapping("/public/books/search")
    public ResponseEntity<Page<BookDTO>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String language,
            @RequestParam(required = false, defaultValue = "false") boolean onlyAvailable,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        
        Page<BookDTO> books = bookService.searchBooks(
                title, isbn, author, category, year, language, onlyAvailable, pageable);
        
        return ResponseEntity.ok(books);
    }

    /**
     * Obtener un libro por ID
     * Accesible para todos los usuarios
     */
    @GetMapping("/public/books/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        BookDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    /**
     * Crear un nuevo libro
     * Solo para bibliotecarios y administradores
     */
    @PostMapping("/admin/books")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<BookDTO> createBook(
            @Valid @RequestBody BookCreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        BookDTO createdBook = bookService.createBook(request, currentUser);
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }

    /**
     * Actualizar un libro existente
     * Solo para bibliotecarios y administradores
     */
    @PutMapping("/admin/books/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<BookDTO> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        BookDTO updatedBook = bookService.updateBook(id, request, currentUser);
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * Eliminar un libro
     * Solo para administradores
     */
    @DeleteMapping("/admin/books/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Actualizar el inventario de un libro (copias totales y disponibles)
     * Solo para bibliotecarios y administradores
     */
    @PutMapping("/admin/books/{id}/inventory")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<BookDTO> updateBookInventory(
            @PathVariable Long id,
            @RequestParam Integer totalCopies,
            @RequestParam Integer availableCopies,
            @AuthenticationPrincipal User currentUser) {
        
        BookDTO updatedBook = bookService.updateBookInventory(id, totalCopies, availableCopies, currentUser);
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * Obtener libros recién añadidos
     * Accesible para todos los usuarios
     */
    @GetMapping("/public/books/recent")
    public ResponseEntity<Page<BookDTO>> getRecentBooks(
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<BookDTO> books = bookService.getRecentBooks(pageable);
        return ResponseEntity.ok(books);
    }
}