package uz.nagato.touragency.media.dto;

import uz.nagato.touragency.media.entity.Media;
import uz.nagato.touragency.media.entity.OwnerType;

import java.time.Instant;
import java.util.Locale;

/**
 * Media library item.
 * <p>
 * Several values are carried under two names ({@code filename}/{@code originalName},
 * {@code alt}/{@code altText}, {@code size}/{@code sizeBytes}): the short forms are what
 * the admin UI reads, the long forms keep existing API consumers working.
 */
public record MediaResponse(
        Long id,
        String filename,
        String fileName,
        String originalName,
        String title,
        String type,
        String mimeType,
        String contentType,
        long size,
        long sizeBytes,
        String url,
        String thumbnailUrl,
        String alt,
        String altText,
        OwnerType ownerType,
        Long ownerId,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt
) {

    public static MediaResponse from(Media media) {
        String type = typeOf(media.getContentType());
        return new MediaResponse(
                media.getId(),
                media.getOriginalName(),
                media.getFileName(),
                media.getOriginalName(),
                media.getTitle(),
                type,
                media.getContentType(),
                media.getContentType(),
                media.getSizeBytes(),
                media.getSizeBytes(),
                media.getUrl(),
                // No derivative is generated; an image is its own thumbnail.
                "image".equals(type) ? media.getUrl() : null,
                media.getAltText(),
                media.getAltText(),
                media.getOwnerType(),
                media.getOwnerId(),
                media.getSortOrder(),
                media.getCreatedAt(),
                media.getUpdatedAt()
        );
    }

    /** Coarse bucket the media library filters on. */
    public static String typeOf(String contentType) {
        if (contentType == null) {
            return "file";
        }
        String value = contentType.toLowerCase(Locale.ENGLISH);
        if (value.startsWith("image/")) {
            return "image";
        }
        if (value.startsWith("video/")) {
            return "video";
        }
        return "file";
    }
}
