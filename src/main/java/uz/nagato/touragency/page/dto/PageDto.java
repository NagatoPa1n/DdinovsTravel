package uz.nagato.touragency.page.dto;

import uz.nagato.touragency.page.entity.Page;

import java.time.Instant;
import java.util.Map;

/**
 * Content page.
 * <p>
 * {@code status} ("published"/"draft") is the string form of {@code published}, and
 * {@code seoTitle}/{@code seoDescription} mirror {@code metaTitle}/{@code metaDescription}.
 * Both spellings are emitted so the admin UI and existing API consumers each find what
 * they expect.
 */
public record PageDto(
        Long id,
        String slug,
        String title,
        String body,
        Map<String, Object> content,
        String excerpt,
        String seoTitle,
        String seoDescription,
        String metaTitle,
        String metaDescription,
        String status,
        boolean published,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt
) {

    public static PageDto from(Page page) {
        return new PageDto(
                page.getId(),
                page.getSlug(),
                page.getTitle(),
                page.getBody(),
                page.getContent(),
                page.getExcerpt(),
                page.getMetaTitle(),
                page.getMetaDescription(),
                page.getMetaTitle(),
                page.getMetaDescription(),
                page.isPublished() ? "published" : "draft",
                page.isPublished(),
                page.getSortOrder(),
                page.getCreatedAt(),
                page.getUpdatedAt()
        );
    }
}
