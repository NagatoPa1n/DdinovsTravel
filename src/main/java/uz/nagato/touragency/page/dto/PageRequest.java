package uz.nagato.touragency.page.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PageRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 160, message = "Title must be at most 160 characters")
        String title,

        String slug,

        String content,

        @Size(max = 500, message = "Excerpt must be at most 500 characters")
        String excerpt,

        String metaTitle,

        @Size(max = 500, message = "Meta description must be at most 500 characters")
        String metaDescription,

        Boolean published,

        Integer sortOrder
) {
}
