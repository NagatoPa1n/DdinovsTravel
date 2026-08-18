package uz.nagato.touragency.media.dto;

import uz.nagato.touragency.media.entity.OwnerType;

/** Re-attaches an uploaded file to an owner or changes its presentation metadata. */
public record MediaUpdateRequest(
        OwnerType ownerType,
        Long ownerId,
        String altText,
        Integer sortOrder
) {
}
