package uz.nagato.touragency.category.dto;

import uz.nagato.touragency.category.entity.Category;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        String iconUrl,
        boolean active
) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getIconUrl(),
                category.isActive()
        );
    }
}
