package uz.nagato.touragency.destination.dto;

import uz.nagato.touragency.destination.entity.Destination;

public record DestinationResponse(
        Long id,
        String name,
        String slug,
        String country,
        String city,
        String description,
        String coverImageUrl,
        Double latitude,
        Double longitude,
        boolean active
) {

    public static DestinationResponse from(Destination destination) {
        return new DestinationResponse(
                destination.getId(),
                destination.getName(),
                destination.getSlug(),
                destination.getCountry(),
                destination.getCity(),
                destination.getDescription(),
                destination.getCoverImageUrl(),
                destination.getLatitude(),
                destination.getLongitude(),
                destination.isActive()
        );
    }
}
