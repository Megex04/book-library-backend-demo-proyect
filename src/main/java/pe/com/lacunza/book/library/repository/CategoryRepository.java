package pe.com.lacunza.book.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.lacunza.book.library.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
