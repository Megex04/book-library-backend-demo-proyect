package com.lacunza.biblio.system.service;

import com.lacunza.biblio.system.dto.book.AuthorDTO;
import com.lacunza.biblio.system.dto.book.BookCreateRequest;
import com.lacunza.biblio.system.dto.book.BookDTO;
import com.lacunza.biblio.system.dto.book.BookUpdateRequest;
import com.lacunza.biblio.system.dto.book.CategoryDTO;
import com.lacunza.biblio.system.exception.ResourceNotFoundException;
import com.lacunza.biblio.system.model.Author;
import com.lacunza.biblio.system.model.Book;
import com.lacunza.biblio.system.model.Category;
import com.lacunza.biblio.system.model.Loan;
import com.lacunza.biblio.system.model.Reservation;
import com.lacunza.biblio.system.model.User;
import com.lacunza.biblio.system.repository.AuthorRepository;
import com.lacunza.biblio.system.repository.BookRepository;
import com.lacunza.biblio.system.repository.CategoryRepository;
import com.lacunza.biblio.system.repository.LoanRepository;
import com.lacunza.biblio.system.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;

    /**
     * Obtener todos los libros (paginados)
     */
    @Transactional(readOnly = true)
    public Page<BookDTO> getAllBooks(Pageable pageable) {
        Page<Book> booksPage = bookRepository.findAll(pageable);
        return booksPage.map(this::convertToDTO);
    }

    /**
     * Buscar libros por varios criterios
     */
    @Transactional(readOnly = true)
    public Page<BookDTO> searchBooks(
            String title, String isbn, String authorName, String categoryName,
            Integer year, String language, boolean onlyAvailable, Pageable pageable) {
        
        Page<Book> booksPage = bookRepository.findByAdvancedSearch(
                title, isbn, authorName, categoryName, year, language, onlyAvailable, pageable);
        
        return booksPage.map(this::convertToDTO);
    }

    /**
     * Obtener un libro por ID
     */
    @Transactional(readOnly = true)
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        
        return convertToDTO(book);
    }

    /**
     * Crear un nuevo libro
     */
    @Transactional
    public BookDTO createBook(BookCreateRequest request, User currentUser) {
        // Verificar si ya existe un libro con el mismo ISBN
        if (request.getIsbn() != null && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new IllegalStateException("Ya existe un libro con el ISBN: " + request.getIsbn());
        }
        
        // Crear el libro
        Book book = Book.builder()
                .title(request.getTitle())
                .isbn(request.getIsbn())
                .publicationYear(request.getPublicationYear())
                .edition(request.getEdition())
                .publisher(request.getPublisher())
                .language(request.getLanguage())
                .physicalLocation(request.getPhysicalLocation())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getAvailableCopies())
                .description(request.getDescription())
                .coverImageUrl(request.getCoverImageUrl())
                .authors(new HashSet<>())
                .categories(new HashSet<>())
                .build();
        
        // Añadir autores
        if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
            Set<Author> authors = request.getAuthorIds().stream()
                    .map(authorId -> authorRepository.findById(authorId)
                            .orElseThrow(() -> new ResourceNotFoundException("Author", "id", authorId)))
                    .collect(Collectors.toSet());
            
            book.setAuthors(authors);
        }
        
        // Añadir categorías
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = request.getCategoryIds().stream()
                    .map(categoryId -> categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId)))
                    .collect(Collectors.toSet());
            
            book.setCategories(categories);
        }
        
        Book savedBook = bookRepository.save(book);
        
        return convertToDTO(savedBook);
    }

    /**
     * Actualizar un libro existente
     */
    @Transactional
    public BookDTO updateBook(Long id, BookUpdateRequest request, User currentUser) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        
        // Verificar si el ISBN ya está en uso por otro libro
        if (request.getIsbn() != null && !request.getIsbn().equals(book.getIsbn()) && 
                bookRepository.existsByIsbn(request.getIsbn())) {
            throw new IllegalStateException("Ya existe un libro con el ISBN: " + request.getIsbn());
        }
        
        // Actualizar los campos del libro
        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setPublicationYear(request.getPublicationYear());
        book.setEdition(request.getEdition());
        book.setPublisher(request.getPublisher());
        book.setLanguage(request.getLanguage());
        book.setPhysicalLocation(request.getPhysicalLocation());
        book.setDescription(request.getDescription());
        book.setCoverImageUrl(request.getCoverImageUrl());
        
        // Actualizar autores
        if (request.getAuthorIds() != null) {
            Set<Author> authors = request.getAuthorIds().stream()
                    .map(authorId -> authorRepository.findById(authorId)
                            .orElseThrow(() -> new ResourceNotFoundException("Author", "id", authorId)))
                    .collect(Collectors.toSet());
            
            book.setAuthors(authors);
        }
        
        // Actualizar categorías
        if (request.getCategoryIds() != null) {
            Set<Category> categories = request.getCategoryIds().stream()
                    .map(categoryId -> categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId)))
                    .collect(Collectors.toSet());
            
            book.setCategories(categories);
        }
        
        Book updatedBook = bookRepository.save(book);
        
        return convertToDTO(updatedBook);
    }

    /**
     * Eliminar un libro
     */
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        
        // Verificar si el libro tiene préstamos activos
        boolean hasActiveLoans = loanRepository.existsByBookAndStatusIn(
                book, Arrays.asList(Loan.LoanStatus.ACTIVE, Loan.LoanStatus.OVERDUE));
        
        if (hasActiveLoans) {
            throw new IllegalStateException("No se puede eliminar un libro con préstamos activos");
        }
        
        // Verificar si el libro tiene reservas pendientes
        boolean hasPendingReservations = reservationRepository.existsByBookAndStatusIn(
                book, Arrays.asList(Reservation.ReservationStatus.PENDING, Reservation.ReservationStatus.READY));
        
        if (hasPendingReservations) {
            throw new IllegalStateException("No se puede eliminar un libro con reservas pendientes");
        }
        
        bookRepository.delete(book);
    }

    /**
     * Actualizar el inventario de un libro
     */
    @Transactional
    public BookDTO updateBookInventory(Long id, Integer totalCopies, Integer availableCopies, User currentUser) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        
        // Validar que el número de copias disponibles no sea mayor que el total
        if (availableCopies > totalCopies) {
            throw new IllegalArgumentException("El número de copias disponibles no puede ser mayor que el total");
        }
        
        book.setTotalCopies(totalCopies);
        book.setAvailableCopies(availableCopies);
        
        Book updatedBook = bookRepository.save(book);
        
        // Si hay nuevas copias disponibles, procesar reservas pendientes
        if (availableCopies > 0) {
            List<Reservation> pendingReservations = reservationRepository.findByBookAndStatus(
                    book, Reservation.ReservationStatus.PENDING);
            
            if (!pendingReservations.isEmpty()) {
                // Procesar la primera reserva pendiente
                Reservation nextReservation = pendingReservations.get(0);
                nextReservation.markAsReady(currentUser);
                reservationRepository.save(nextReservation);
            }
        }
        
        return convertToDTO(updatedBook);
    }

    /**
     * Obtener libros recién añadidos
     */
    @Transactional(readOnly = true)
    public Page<BookDTO> getRecentBooks(Pageable pageable) {
        Page<Book> booksPage = bookRepository.findAllByOrderByCreatedAtDesc(pageable);
        return booksPage.map(this::convertToDTO);
    }

    /**
     * Convertir entidad Book a DTO
     */
    public BookDTO convertToDTO(Book book) {
        // Contar préstamos y reservas activas
        long loanCount = loanRepository.countByBookAndStatusIn(
                book, Arrays.asList(Loan.LoanStatus.ACTIVE, Loan.LoanStatus.OVERDUE));
        
        long reservationCount = reservationRepository.countByBookAndStatusIn(
                book, Arrays.asList(Reservation.ReservationStatus.PENDING, Reservation.ReservationStatus.READY));
        
        // Materializamos las colecciones lazy (PersistentSet) a listas nuevas
        // ANTES de iterarlas. Al buscar libros con JOIN + DISTINCT (findByAdvancedSearch),
        // Hibernate hace fetch en lote de estas colecciones para todos los libros
        // de la página; iterar directamente el PersistentSet mientras Hibernate
        // todavía está inyectando su estado cargado provoca un
        // ConcurrentModificationException intermitente. Copiar primero con
        // new ArrayList<>(...) evita compartir esa colección "viva".
        List<Author> authorsSnapshot = new ArrayList<>(book.getAuthors());
        List<Category> categoriesSnapshot = new ArrayList<>(book.getCategories());

        // Convertir autores a DTOs
        Set<AuthorDTO> authorDTOs = authorsSnapshot.stream()
                .map(author -> AuthorDTO.builder()
                        .id(author.getId())
                        .name(author.getName())
                        .biography(author.getBiography())
                        .birthYear(author.getBirthYear())
                        .deathYear(author.getDeathYear())
                        .nationality(author.getNationality())
                        .build())
                .collect(Collectors.toSet());

        // Convertir categorías a DTOs
        Set<CategoryDTO> categoryDTOs = categoriesSnapshot.stream()
                .map(category -> CategoryDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .parentId(category.getParent() != null ? category.getParent().getId() : null)
                        .parentName(category.getParent() != null ? category.getParent().getName() : null)
                        .build())
                .collect(Collectors.toSet());
        
        return BookDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .publicationYear(book.getPublicationYear())
                .edition(book.getEdition())
                .publisher(book.getPublisher())
                .language(book.getLanguage())
                .physicalLocation(book.getPhysicalLocation())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .description(book.getDescription())
                .coverImageUrl(book.getCoverImageUrl())
                .authors(authorDTOs)
                .categories(categoryDTOs)
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .available(book.getAvailableCopies() > 0)
                .reserved(reservationCount > 0)
                .reservationCount(reservationCount)
                .loanCount(loanCount)
                .build();
    }
}