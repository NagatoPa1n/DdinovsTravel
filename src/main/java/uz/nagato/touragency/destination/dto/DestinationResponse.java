package uz.nagato.touragency.destination.dto;

import uz.nagato.touragency.common.dto.MediaRef;
import uz.nagato.touragency.destination.entity.Destination;

import java.time.Instant;

public record DestinationResponse(
        Long id,
        String name,
        String slug,
        String country,
        String city,
        String description,
        String coverImageUrl,
        MediaRef image,
        boolean featured,
        Double latitude,
        Double longitude,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static DestinationResponse from(Destination destination) {
        if (destination == null) {
            return null;
        }
        return new DestinationResponse(
                destination.getId(),
                destination.getName(),
                destination.getSlug(),
                destination.getCountry(),
                destination.getCity(),
                destination.getDescription(),
                // Falls back to the embedded image so older consumers still get a URL.
                destination.getCoverImageUrl() != null
                        ? destination.getCoverImageUrl()
                        : (destination.getImage() == null ? null : destination.getImage().url()),
                destination.getImage(),
                destination.isFeatured(),
                destination.getLatitude(),
                destination.getLongitude(),
                destination.isActive(),
                destination.getCreatedAt(),
                destination.getUpdatedAt()
        );
    }
}
