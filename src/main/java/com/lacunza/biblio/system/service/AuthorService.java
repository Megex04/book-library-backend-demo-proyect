package com.lacunza.biblio.system.service;

import com.lacunza.biblio.system.dto.book.AuthorDTO;
import com.lacunza.biblio.system.model.Author;
import com.lacunza.biblio.system.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de solo lectura para autores. Se reintrodujo únicamente para
 * poder listar autores desde el frontend (combo box en la edición de
 * libros); no expone create/update/delete porque el frontend no los
 * necesita todavía.
 */
@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;

    @Transactional(readOnly = true)
    public Page<AuthorDTO> getAllAuthors(Pageable pageable) {
        return authorRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<AuthorDTO> searchAuthorsByName(String name, Pageable pageable) {
        return authorRepository.findByNameContainingIgnoreCase(name, pageable).map(this::convertToDTO);
    }

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
