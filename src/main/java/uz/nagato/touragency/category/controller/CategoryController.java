package uz.nagato.touragency.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.nagato.touragency.category.dto.CategoryRequest;
import uz.nagato.touragency.category.dto.CategoryResponse;
import uz.nagato.touragency.category.service.CategoryService;
import uz.nagato.touragency.translation.RequestLanguage;
import uz.nagato.touragency.translation.service.ContentTranslator;
import uz.nagato.touragency.common.response.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final RequestLanguage requestLanguage;
    private final ContentTranslator contentTranslator;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> list(
            @RequestParam(defaultValue = "true") boolean onlyActive,
            @RequestParam(required = false) String lang,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        List<CategoryResponse> categories = categoryService.findAll(onlyActive);
        return ApiResponse.ok(contentTranslator.translateCategories(
                categories, requestLanguage.resolve(lang, acceptLanguage)));
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> byId(@PathVariable Long id) {
        return ApiResponse.ok(categoryService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<CategoryResponse> bySlug(
            @PathVariable String slug,
            @RequestParam(required = false) String lang,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        CategoryResponse category = categoryService.findBySlug(slug);
        return ApiResponse.ok(
                contentTranslator.translateCategory(category, requestLanguage.resolve(lang, acceptLanguage)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ApiResponse.ok("Category created", categoryService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<CategoryResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.ok("Category updated", categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.message("Category deleted");
    }
}
