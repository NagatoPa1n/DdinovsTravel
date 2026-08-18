package uz.nagato.touragency.media.dto;

import uz.nagato.touragency.media.entity.Media;
import uz.nagato.touragency.media.entity.OwnerType;

public record MediaResponse(
        Long id,
        String fileName,
        String originalName,
        String contentType,
        long sizeBytes,
        String url,
        OwnerType ownerType,
        Long ownerId,
        String altText,
        int sortOrder
) {

    public static MediaResponse from(Media media) {
        return new MediaResponse(
                media.getId(),
                media.getFileName(),
                media.getOriginalName(),
                media.getContentType(),
                media.getSizeBytes(),
                media.getUrl(),
                media.getOwnerType(),
                media.getOwnerId(),
                media.getAltText(),
                media.getSortOrder()
        );
    }
}
