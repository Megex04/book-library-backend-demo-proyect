package com.lacunza.biblio.system.service;

import com.lacunza.biblio.system.dto.book.AuthorDTO;
import com.lacunza.biblio.system.dto.book.BookDTO;
import com.lacunza.biblio.system.exception.ResourceNotFoundException;
import com.lacunza.biblio.system.model.Author;
import com.lacunza.biblio.system.model.Book;
import com.lacunza.biblio.system.repository.AuthorRepository;
import com.lacunza.biblio.system.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;

    /**
     * Obtener todos los autores (paginados)
     */
    @Transactional(readOnly = true)
    public Page<AuthorDTO> getAllAuthors(Pageable pageable) {
        Page<Author> authorsPage = authorRepository.findAll(pageable);
        return authorsPage.map(this::convertToDTO);
    }

    /**
     * Buscar autores por nombre
     */
    @Transactional(readOnly = true)
    public Page<AuthorDTO> searchAuthorsByName(String name, Pageable pageable) {
        Page<Author> authorsPage = authorRepository.findByNameContainingIgnoreCase(name, pageable);
        return authorsPage.map(this::convertToDTO);
    }

    /**
     * Obtener un autor por ID
     */
    @Transactional(readOnly = true)
    public AuthorDTO getAuthorById(Long id) {
        Author author = findAuthorById(id);
        return convertToDTO(author);
    }

    /**
     * Obtener libros de un autor
     */
    @Transactional(readOnly = true)
    public Page<BookDTO> getAuthorBooks(Long authorId, Pageable pageable) {
        Author author = findAuthorById(authorId);
        
        // Obtener los libros del autor
        List<Book> books = bookRepository.findByAuthorsContaining(author);
        
        // Convertir a DTOs y paginar
        List<BookDTO> bookDTOs = books.stream()
                .map(bookService::convertToDTO)
                .collect(Collectors.toList());
        
        // Crear una página a partir de la lista
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), bookDTOs.size());
        
        List<BookDTO> pageContent = bookDTOs.subList(start, end);
        return new PageImpl<>(pageContent, pageable, bookDTOs.size());
    }

    /**
     * Crear un nuevo autor
     */
    @Transactional
    public AuthorDTO createAuthor(AuthorDTO authorDTO) {
        // Verificar si ya existe un autor con el mismo nombre
        if (authorRepository.existsByNameIgnoreCase(authorDTO.getName())) {
            throw new IllegalStateException("Ya existe un autor con el nombre: " + authorDTO.getName());
        }
        
        Author author = new Author();
        updateAuthorFromDTO(author, authorDTO);
        
        // Establecer fechas de creación y actualización
        LocalDateTime now = LocalDateTime.now();
        author.setCreatedAt(now);
        author.setUpdatedAt(now);
        
        Author savedAuthor = authorRepository.save(author);
        return convertToDTO(savedAuthor);
    }

    /**
     * Actualizar un autor existente
     */
    @Transactional
    public AuthorDTO updateAuthor(Long id, AuthorDTO authorDTO) {
        Author author = findAuthorById(id);
        
        // Verificar si ya existe otro autor con el mismo nombre
        if (!author.getName().equalsIgnoreCase(authorDTO.getName()) && 
                authorRepository.existsByNameIgnoreCase(authorDTO.getName())) {
            throw new IllegalStateException("Ya existe un autor con el nombre: " + authorDTO.getName());
        }
        
        updateAuthorFromDTO(author, authorDTO);
        
        // Actualizar fecha de modificación
        author.setUpdatedAt(LocalDateTime.now());
        
        Author updatedAuthor = authorRepository.save(author);
        return convertToDTO(updatedAuthor);
    }

    /**
     * Eliminar un autor
     */
    @Transactional
    public void deleteAuthor(Long id) {
        Author author = findAuthorById(id);
        
        // Verificar si el autor tiene libros asociados
        if (!author.getBooks().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar el autor porque tiene libros asociados");
        }
        
        authorRepository.delete(author);
    }

    /**
     * Obtener autores más prolíficos (con más libros)
     */
    @Transactional(readOnly = true)
    public Page<AuthorDTO> getMostProlificAuthors(Pageable pageable) {
        Page<Author> authorsPage = authorRepository.findMostProlificAuthors(pageable);
        return authorsPage.map(this::convertToDTO);
    }

    /**
     * Método auxiliar para buscar un autor por ID
     */
    private Author findAuthorById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado con ID: " + id));
    }

    /**
     * Método auxiliar para actualizar un autor a partir de un DTO
     */
    private void updateAuthorFromDTO(Author author, AuthorDTO authorDTO) {
        author.setName(authorDTO.getName());
        author.setBiography(authorDTO.getBiography());
        author.setBirthYear(authorDTO.getBirthYear());
        author.setDeathYear(authorDTO.getDeathYear());
        author.setNationality(authorDTO.getNationality());
    }

    /**
     * Método auxiliar para convertir un Author a AuthorDTO
     */
    public AuthorDTO convertToDTO(Author author) {
        return AuthorDTO.builder()
                .id(author.getId())
                .name(author.getName())
                .biography(author.getBiography())
                .birthYear(author.getBirthYear())
                .deathYear(author.getDeathYear())
                .nationality(author.getNationality())
                .build();
    }
}