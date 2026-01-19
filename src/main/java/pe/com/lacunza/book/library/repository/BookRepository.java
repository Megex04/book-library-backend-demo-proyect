package pe.com.lacunza.book.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.com.lacunza.book.library.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    // Búsqueda simple por título (ignorando mayúsculas/minúsculas)
    List<Book> findByTitleContainingIgnoreCase(String title);

    // Buscar por ID de categoría
    List<Book> findByCategoryId(Long categoryId);

    // Query personalizada: Buscar libros disponibles
    @Query("SELECT b FROM Book b WHERE b.stock > 0")
    List<Book> findAllAvailable();
}
