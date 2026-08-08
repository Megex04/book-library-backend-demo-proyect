package com.lacunza.biblio.system.service;

import com.lacunza.biblio.system.dto.book.BookDTO;
import com.lacunza.biblio.system.dto.book.CategoryDTO;
import com.lacunza.biblio.system.exception.ResourceNotFoundException;
import com.lacunza.biblio.system.model.Book;
import com.lacunza.biblio.system.model.Category;
import com.lacunza.biblio.system.repository.BookRepository;
import com.lacunza.biblio.system.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;

    /**
     * Obtener todas las categorías (paginadas)
     */
    @Transactional(readOnly = true)
    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        Page<Category> categoriesPage = categoryRepository.findAll(pageable);
        return categoriesPage.map(this::convertToDTO);
    }

    /**
     * Obtener todas las categorías principales (sin categoría padre)
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getMainCategories() {
        List<Category> mainCategories = categoryRepository.findByParentIsNull();
        return mainCategories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Buscar categorías por nombre
     */
    @Transactional(readOnly = true)
    public Page<CategoryDTO> searchCategoriesByName(String name, Pageable pageable) {
        Page<Category> categoriesPage = categoryRepository.findByNameContainingIgnoreCase(name, pageable);
        return categoriesPage.map(this::convertToDTO);
    }

    /**
     * Obtener una categoría por ID
     */
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        Category category = findCategoryById(id);
        return convertToDTO(category);
    }

    /**
     * Obtener subcategorías de una categoría
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getSubcategories(Long categoryId) {
        // Verificar que la categoría existe
        findCategoryById(categoryId);
        
        List<Category> subcategories = categoryRepository.findByParentId(categoryId);
        return subcategories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener libros de una categoría
     */
    @Transactional(readOnly = true)
    public Page<BookDTO> getCategoryBooks(Long categoryId, Pageable pageable) {
        Category category = findCategoryById(categoryId);
        
        // Obtener los libros de la categoría
        List<Book> books = bookRepository.findByCategoriesContaining(category);
        
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
     * Crear una nueva categoría
     */
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        // Verificar si ya existe una categoría con el mismo nombre
        if (categoryRepository.existsByNameIgnoreCase(categoryDTO.getName())) {
            throw new IllegalStateException("Ya existe una categoría con el nombre: " + categoryDTO.getName());
        }
        
        Category category = new Category();
        updateCategoryFromDTO(category, categoryDTO);
        
        // Establecer fechas de creación y actualización
        LocalDateTime now = LocalDateTime.now();
        category.setCreatedAt(now);
        category.setUpdatedAt(now);
        
        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    /**
     * Actualizar una categoría existente
     */
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = findCategoryById(id);
        
        // Verificar si ya existe otra categoría con el mismo nombre
        if (!category.getName().equalsIgnoreCase(categoryDTO.getName()) && 
                categoryRepository.existsByNameIgnoreCase(categoryDTO.getName())) {
            throw new IllegalStateException("Ya existe una categoría con el nombre: " + categoryDTO.getName());
        }
        
        // Verificar que no se esté creando un ciclo en la jerarquía de categorías
        if (categoryDTO.getParentId() != null) {
            if (categoryDTO.getParentId().equals(id)) {
                throw new IllegalArgumentException("Una categoría no puede ser su propia categoría padre");
            }
            
            // Verificar que no se esté asignando como padre a una de sus subcategorías
            if (isSubcategoryOf(categoryDTO.getParentId(), id)) {
                throw new IllegalArgumentException("No se puede asignar como categoría padre a una subcategoría");
            }
        }
        
        updateCategoryFromDTO(category, categoryDTO);
        
        // Actualizar fecha de modificación
        category.setUpdatedAt(LocalDateTime.now());
        
        Category updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    /**
     * Eliminar una categoría
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategoryById(id);
        
        // Verificar si la categoría tiene subcategorías
        List<Category> subcategories = categoryRepository.findByParentId(id);
        if (!subcategories.isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la categoría porque tiene subcategorías asociadas");
        }
        
        // Verificar si la categoría tiene libros asociados
        if (!category.getBooks().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la categoría porque tiene libros asociados");
        }
        
        categoryRepository.delete(category);
    }

    /**
     * Obtener categorías más populares (con más libros)
     */
    @Transactional(readOnly = true)
    public Page<CategoryDTO> getMostPopularCategories(Pageable pageable) {
        Page<Category> categoriesPage = categoryRepository.findMostPopularCategories(pageable);
        return categoriesPage.map(this::convertToDTO);
    }

    /**
     * Método auxiliar para buscar una categoría por ID
     */
    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
    }

    /**
     * Método auxiliar para actualizar una categoría a partir de un DTO
     */
    private void updateCategoryFromDTO(Category category, CategoryDTO categoryDTO) {
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        
        // Establecer categoría padre si se proporciona
        if (categoryDTO.getParentId() != null) {
            Category parent = findCategoryById(categoryDTO.getParentId());
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
    }

    /**
     * Método auxiliar para convertir una Category a CategoryDTO
     */
    public CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
        
        // Agregar información de la categoría padre si existe
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }
        
        // Agregar subcategorías si existen.
        // Materializamos a una lista nueva antes de iterar: si esta colección
        // lazy se resuelve mientras Hibernate está haciendo fetch en lote de
        // otras colecciones de la misma consulta (p. ej. al listar varias
        // categorías paginadas), iterar el PersistentSet "en vivo" puede
        // lanzar ConcurrentModificationException.
        List<Category> subcategoriesSnapshot = category.getSubcategories() != null
                ? new ArrayList<>(category.getSubcategories())
                : List.of();

        if (!subcategoriesSnapshot.isEmpty()) {
            Set<CategoryDTO> subcategoryDTOs = subcategoriesSnapshot.stream()
                    .map(subcategory -> CategoryDTO.builder()
                            .id(subcategory.getId())
                            .name(subcategory.getName())
                            .description(subcategory.getDescription())
                            .parentId(category.getId())
                            .parentName(category.getName())
                            .build())
                    .collect(Collectors.toSet());
            
            dto.setSubcategories(subcategoryDTOs);
        }
        
        return dto;
    }

    /**
     * Método auxiliar para verificar si una categoría es subcategoría de otra
     */
    private boolean isSubcategoryOf(Long potentialSubcategoryId, Long categoryId) {
        Category potentialSubcategory = findCategoryById(potentialSubcategoryId);
        
        // Recorrer la jerarquía de categorías hacia arriba
        Category current = potentialSubcategory;
        while (current.getParent() != null) {
            if (current.getParent().getId().equals(categoryId)) {
                return true;
            }
            current = current.getParent();
        }
        
        return false;
    }
}