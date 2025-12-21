package course.service;

import course.dto.CategoryRequest;
import course.dto.CategoryResponse;
import course.exception.ResourceNotFoundException;
import course.model.Category;
import course.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByDeletedAtIsNull().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(String id) {
        Category category = categoryRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Category with name " + request.getName() + " already exists");
        }
        if (categoryRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Category with code " + request.getCode() + " already exists");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setDescription(request.getDescription());

        Category savedCategory = categoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(String id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Category with name " + request.getName() + " already exists");
        }
        if (!category.getCode().equals(request.getCode()) && categoryRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Category with code " + request.getCode() + " already exists");
        }

        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setDescription(request.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        return mapToResponse(updatedCategory);
    }

    @Transactional
    public void deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setDeletedAt(java.time.LocalDateTime.now());
        categoryRepository.save(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getCode(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
