package uz.nagato.touragency.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.nagato.touragency.category.dto.CategoryRequest;
import uz.nagato.touragency.category.dto.CategoryResponse;
import uz.nagato.touragency.category.entity.Category;
import uz.nagato.touragency.category.repository.CategoryRepository;
import uz.nagato.touragency.common.exception.ConflictException;
import uz.nagato.touragency.common.exception.NotFoundException;
import uz.nagato.touragency.common.util.SlugUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> findAll(boolean onlyActive) {
        List<Category> categories = onlyActive
                ? categoryRepository.findAllByActiveTrueOrderByNameAsc()
                : categoryRepository.findAll();
        return categories.stream().map(CategoryResponse::from).toList();
    }

    public CategoryResponse findById(Long id) {
        return CategoryResponse.from(getEntity(id));
    }

    public CategoryResponse findBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Category not found with slug: " + slug));
        return CategoryResponse.from(category);
    }

    /** Entity lookup for other modules (tours reference categories). */
    public Category getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Category", id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String slug = SlugUtils.slugOrDerive(request.slug(), request.name());
        if (categoryRepository.existsBySlug(slug)) {
            throw new ConflictException("Category slug already in use: " + slug);
        }
        Category category = new Category();
        apply(category, request, slug);
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getEntity(id);
        String slug = SlugUtils.slugOrDerive(request.slug(), request.name());
        if (categoryRepository.existsBySlugAndIdNot(slug, id)) {
            throw new ConflictException("Category slug already in use: " + slug);
        }
        apply(category, request, slug);
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.delete(getEntity(id));
    }

    private void apply(Category category, CategoryRequest request, String slug) {
        category.setName(request.name());
        category.setSlug(slug);
        category.setDescription(request.description());
        category.setIconUrl(request.iconUrl());
        category.setActive(request.active() == null || request.active());
    }
}
