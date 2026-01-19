package pe.com.lacunza.book.library.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.com.lacunza.book.library.dto.book.BookRequest;
import pe.com.lacunza.book.library.dto.book.BookResponse;
import pe.com.lacunza.book.library.service.BookService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // GET: Accesible por cualquier usuario autenticado (MEMBER, LIBRARIAN, ADMIN)
    // No hace falta @PreAuthorize si en SecurityConfig pusiste .anyRequest().authenticated()
    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    // POST: Solo Admin y Bibliotecario
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BookResponse> createBook(@RequestBody @Valid BookRequest request) {
        return ResponseEntity.ok(bookService.createBook(request));
    }

    // PUT: Solo Admin y Bibliotecario
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BookResponse> updateBook(@PathVariable Long id, @RequestBody @Valid BookRequest request) {
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    // DELETE: Solo Admin (Nivel más alto)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
