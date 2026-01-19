package pe.com.lacunza.book.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.lacunza.book.library.dto.book.BookRequest;
import pe.com.lacunza.book.library.dto.book.BookResponse;
import pe.com.lacunza.book.library.model.Book;
import pe.com.lacunza.book.library.model.Category;
import pe.com.lacunza.book.library.repository.BookRepository;
import pe.com.lacunza.book.library.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    // --- LECTURA (Público / Miembros) ---

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));
        return mapToResponse(book);
    }

    // --- ESCRITURA (Admin / Bibliotecario) ---

    @Transactional
    public BookResponse createBook(BookRequest request) {
        // 1. Validar que la categoría exista
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // 2. Validar ISBN único (Opcional, pero recomendado)
        if (bookRepository.findByIsbn(request.isbn()).isPresent()) {
            throw new RuntimeException("Ya existe un libro con ese ISBN");
        }

        // 3. Crear Entidad
        Book book = Book.builder()
                .isbn(request.isbn())
                .title(request.title())
                .author(request.author())
                .publisher(request.publisher())
                .publicationDate(request.publicationDate())
                .stock(request.stock())
                .category(category)
                .available(request.stock() > 0)
                .build();

        // 4. Guardar y retornar
        Book savedBook = bookRepository.save(book);
        return mapToResponse(savedBook);
    }

    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // Actualizamos campos
        book.setIsbn(request.isbn());
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setPublisher(request.publisher());
        book.setPublicationDate(request.publicationDate());
        book.setStock(request.stock());
        book.setCategory(category);

        // El método @PreUpdate en la entidad se encargará de actualizar 'available'
        // pero si quieres forzarlo: book.setAvailable(request.stock() > 0);

        Book updatedBook = bookRepository.save(book);
        return mapToResponse(updatedBook);
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Libro no encontrado");
        }
        bookRepository.deleteById(id);
    }

    // --- Mapper Auxiliar ---
    private BookResponse mapToResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getCategory().getName(), // Devolvemos el nombre de la categoría
                book.getStock(),
                book.isAvailable()
        );
    }
}
