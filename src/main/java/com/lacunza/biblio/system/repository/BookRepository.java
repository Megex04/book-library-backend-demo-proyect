package com.lacunza.biblio.system.repository;

import com.lacunza.biblio.system.model.Author;
import com.lacunza.biblio.system.model.Book;
import com.lacunza.biblio.system.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    // Buscar libro por ISBN
    Optional<Book> findByIsbn(String isbn);
    
    // Verificar si existe un libro con el ISBN dado
    boolean existsByIsbn(String isbn);
    
    // Buscar libros por título (coincidencia parcial, ignorando mayúsculas/minúsculas)
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    // Buscar libros por autor
    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a = :author")
    Page<Book> findByAuthor(@Param("author") Author author, Pageable pageable);
    
    // Buscar libros por categoría (incluyendo subcategorías)
    @Query("SELECT DISTINCT b FROM Book b JOIN b.categories c WHERE c = :category OR c.parent = :category")
    Page<Book> findByCategory(@Param("category") Category category, Pageable pageable);
    
    // Buscar libros por año de publicación
    Page<Book> findByPublicationYear(Integer year, Pageable pageable);
    
    // Buscar libros por idioma
    Page<Book> findByLanguageIgnoreCase(String language, Pageable pageable);
    
    // Buscar libros con copias disponibles
    Page<Book> findByAvailableCopiesGreaterThan(Integer copies, Pageable pageable);
    
    // Búsqueda avanzada combinando varios criterios
    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN b.authors a LEFT JOIN b.categories c " +
           "WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:isbn IS NULL OR b.isbn = :isbn) " +
           "AND (:authorName IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%'))) " +
           "AND (:categoryName IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))) " +
           "AND (:year IS NULL OR b.publicationYear = :year) " +
           "AND (:language IS NULL OR LOWER(b.language) = LOWER(:language)) " +
           "AND (:available = false OR b.availableCopies > 0)")
    Page<Book> findByAdvancedSearch(
            @Param("title") String title,
            @Param("isbn") String isbn,
            @Param("authorName") String authorName,
            @Param("categoryName") String categoryName,
            @Param("year") Integer year,
            @Param("language") String language,
            @Param("available") boolean onlyAvailable,
            Pageable pageable);
    
    // Buscar libros recién añadidos
    Page<Book> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Contar libros por categoría
    @Query("SELECT COUNT(b) FROM Book b JOIN b.categories c WHERE c = :category")
    long countByCategory(@Param("category") Category category);
    
    // Contar libros por autor
    @Query("SELECT COUNT(b) FROM Book b JOIN b.authors a WHERE a = :author")
    long countByAuthor(@Param("author") Author author);
    
    // Buscar libros que contengan un autor específico
    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a = :author")
    List<Book> findByAuthorsContaining(@Param("author") Author author);
    
    // Buscar libros que pertenecen a una categoría específica
    @Query("SELECT b FROM Book b JOIN b.categories c WHERE c = :category")
    List<Book> findByCategoriesContaining(@Param("category") Category category);
}