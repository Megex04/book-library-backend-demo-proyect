package com.lacunza.biblio.system.repository;

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
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Buscar categoría por nombre
    Optional<Category> findByNameIgnoreCase(String name);
    
    // Verificar si existe una categoría con el mismo nombre
    boolean existsByNameIgnoreCase(String name);
    
    // Buscar categorías por nombre (coincidencia parcial, ignorando mayúsculas/minúsculas)
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Buscar categorías principales (sin padre)
    List<Category> findByParentIsNull();
    
    // Buscar subcategorías de una categoría
    List<Category> findByParentId(Long parentId);
    
    // Buscar categorías con más libros
    @Query("SELECT c FROM Category c JOIN c.books b GROUP BY c ORDER BY COUNT(b) DESC")
    Page<Category> findMostPopularCategories(Pageable pageable);
    
    // Buscar categorías por libro
    @Query("SELECT c FROM Category c JOIN c.books b WHERE b.id = :bookId")
    List<Category> findByBookId(@Param("bookId") Long bookId);
    
    // Contar libros por categoría
    @Query("SELECT COUNT(b) FROM Category c JOIN c.books b WHERE c.id = :categoryId")
    long countBooksByCategoryId(@Param("categoryId") Long categoryId);
}