package pe.com.lacunza.book.library.dto.book;

import java.time.LocalDate;

public record BookRequest(
        String isbn,
        String title,
        String author,
        String publisher,
        LocalDate publicationDate,
        Integer stock,
        Long categoryId // Solo recibimos el ID de la categoría
) {}
