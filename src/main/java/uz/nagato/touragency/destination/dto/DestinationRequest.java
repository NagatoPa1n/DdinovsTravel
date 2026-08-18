package uz.nagato.touragency.destination.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DestinationRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must be at most 120 characters")
        String name,

        String slug,

        @NotBlank(message = "Country is required")
        String country,

        String city,

        String description,

        String coverImageUrl,

        Double latitude,

        Double longitude,

        Boolean active
) {
}
