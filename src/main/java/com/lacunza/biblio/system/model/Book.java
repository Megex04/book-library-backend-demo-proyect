package com.lacunza.biblio.system.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// @EqualsAndHashCode(of = "id") sobrescribe el equals/hashCode que genera @Data
// (que por defecto incluye TODOS los campos, incluidas las colecciones lazy
// authors/categories/loans/reservations). Con relaciones @ManyToMany/@OneToMany
// bidireccionales, dejar que @Data las incluya provoca que Hibernate dispare
// cargas lazy en cascada cada vez que necesita el hashCode de un Book para
// meterlo en un HashSet (p. ej. al construir Author.books o Category.books
// durante un fetch batch), lo que termina en ConcurrentModificationException
// al modificarse una colección mientras otra todavía se está inicializando.
@Entity
@Table(name = "books")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true)
    private String isbn;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "edition")
    private String edition;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "language")
    private String language;

    @Column(name = "physical_location")
    private String physicalLocation;

    @Column(name = "total_copies")
    private Integer totalCopies;

    @Column(name = "available_copies")
    private Integer availableCopies;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @Builder.Default
    private Set<Author> authors = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_categories",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Loan> loans = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Reservation> reservations = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Método para añadir un autor al libro
    public void addAuthor(Author author) {
        authors.add(author);
        author.getBooks().add(this);
    }

    // Método para eliminar un autor del libro
    public void removeAuthor(Author author) {
        authors.remove(author);
        author.getBooks().remove(this);
    }

    // Método para añadir una categoría al libro
    public void addCategory(Category category) {
        categories.add(category);
        category.getBooks().add(this);
    }

    // Método para eliminar una categoría del libro
    public void removeCategory(Category category) {
        categories.remove(category);
        category.getBooks().remove(this);
    }
}