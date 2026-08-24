package uz.nagato.touragency.media.dto;

import uz.nagato.touragency.media.entity.OwnerType;

/**
 * Re-attaches an uploaded file to an owner or changes its presentation metadata.
 * {@code alt} and {@code altText} are accepted interchangeably; whichever is present wins.
 */
public record MediaUpdateRequest(
        OwnerType ownerType,
        Long ownerId,
        String alt,
        String altText,
        String title,
        Integer sortOrder
) {

    /** The alt text to apply, or null when the request did not mention it. */
    public String resolvedAltText() {
        return alt != null ? alt : altText;
    }
}
