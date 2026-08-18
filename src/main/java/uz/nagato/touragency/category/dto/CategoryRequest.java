package uz.nagato.touragency.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must be at most 120 characters")
        String name,

        @Size(max = 140, message = "Slug must be at most 140 characters")
        String slug,

        String description,

        String iconUrl,

        Boolean active
) {
}
