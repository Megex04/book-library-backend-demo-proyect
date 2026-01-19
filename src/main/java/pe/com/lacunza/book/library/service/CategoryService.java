package pe.com.lacunza.book.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.com.lacunza.book.library.dto.category.CategoryRequest;
import pe.com.lacunza.book.library.dto.category.CategoryResponse;
import pe.com.lacunza.book.library.model.Category;
import pe.com.lacunza.book.library.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        return mapToResponse(category);
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        category.setName(request.name());
        category.setDescription(request.description());

        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    public void deleteCategory(Long id) {
        // Opcional: Validar si tiene libros asociados antes de borrar para evitar errores de FK
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada");
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}
