package pe.com.lacunza.book.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String isbn; // Identificador único internacional

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author; // Podría ser otra entidad 'Author', pero String está bien para empezar

    private String publisher; // Editorial

    private LocalDate publicationDate;

    @Column(nullable = false)
    private Integer stock; // Cantidad total de copias

    private boolean available; // true si stock > 0 (se puede calcular, pero útil tenerlo)

    @ManyToOne(fetch = FetchType.EAGER) // EAGER para que traiga la categoría al cargar el libro
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Método útil para actualizar disponibilidad automáticamente
    @PrePersist
    @PreUpdate
    public void updateAvailability() {
        this.available = this.stock > 0;
    }
}
