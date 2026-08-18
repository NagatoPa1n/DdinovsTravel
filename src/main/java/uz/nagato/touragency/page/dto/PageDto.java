package uz.nagato.touragency.page.dto;

import uz.nagato.touragency.page.entity.Page;

import java.time.Instant;

public record PageDto(
        Long id,
        String slug,
        String title,
        String content,
        String excerpt,
        String metaTitle,
        String metaDescription,
        boolean published,
        int sortOrder,
        Instant updatedAt
) {

    public static PageDto from(Page page) {
        return new PageDto(
                page.getId(),
                page.getSlug(),
                page.getTitle(),
                page.getContent(),
                page.getExcerpt(),
                page.getMetaTitle(),
                page.getMetaDescription(),
                page.isPublished(),
                page.getSortOrder(),
                page.getUpdatedAt()
        );
    }
}
