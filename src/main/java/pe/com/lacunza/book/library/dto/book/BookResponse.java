package pe.com.lacunza.book.library.dto.book;

import java.time.LocalDate;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        String author,
        String categoryName, // Devolvemos el nombre, no solo el ID
        Integer stock,
        boolean available
) {}
