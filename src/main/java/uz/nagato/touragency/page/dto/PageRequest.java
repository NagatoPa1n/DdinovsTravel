package uz.nagato.touragency.page.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;
import java.util.Map;

/**
 * Accepts either spelling of the SEO and publication fields, so the admin UI can post its
 * own vocabulary ({@code seoTitle}, {@code status}) without the older names being dropped.
 */
public record PageRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 160, message = "Title must be at most 160 characters")
        String title,

        String slug,

        String body,

        Map<String, Object> content,

        @Size(max = 500, message = "Excerpt must be at most 500 characters")
        String excerpt,

        String seoTitle,

        @Size(max = 500, message = "SEO description must be at most 500 characters")
        String seoDescription,

        String metaTitle,

        @Size(max = 500, message = "Meta description must be at most 500 characters")
        String metaDescription,

        String status,

        Boolean published,

        Integer sortOrder
) {

    public String resolvedMetaTitle() {
        return seoTitle != null ? seoTitle : metaTitle;
    }

    public String resolvedMetaDescription() {
        return seoDescription != null ? seoDescription : metaDescription;
    }

    /** Defaults to published: a page saved from the editor is expected to go live. */
    public boolean resolvedPublished() {
        if (status != null && !status.isBlank()) {
            return !"draft".equals(status.trim().toLowerCase(Locale.ENGLISH));
        }
        return published == null || published;
    }
}
