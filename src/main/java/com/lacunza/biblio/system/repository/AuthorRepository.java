package com.lacunza.biblio.system.repository;

import com.lacunza.biblio.system.model.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    
    // Buscar autores por nombre (coincidencia parcial, ignorando mayúsculas/minúsculas)
    Page<Author> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Buscar autores por nacionalidad
    Page<Author> findByNationalityIgnoreCase(String nationality, Pageable pageable);
    
    // Buscar autores por rango de años de nacimiento
    @Query("SELECT a FROM Author a WHERE a.birthYear BETWEEN :startYear AND :endYear")
    Page<Author> findByBirthYearBetween(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear, Pageable pageable);
    
    // Buscar autores con más libros
    @Query("SELECT a FROM Author a JOIN a.books b GROUP BY a ORDER BY COUNT(b) DESC")
    Page<Author> findMostProlificAuthors(Pageable pageable);
    
    // Verificar si existe un autor con el mismo nombre
    boolean existsByNameIgnoreCase(String name);
    
    // Buscar autores por libro
    @Query("SELECT a FROM Author a JOIN a.books b WHERE b.id = :bookId")
    List<Author> findByBookId(@Param("bookId") Long bookId);
}