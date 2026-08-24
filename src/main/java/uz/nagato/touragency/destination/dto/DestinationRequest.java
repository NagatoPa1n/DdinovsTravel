package uz.nagato.touragency.destination.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import uz.nagato.touragency.common.dto.MediaRef;

/**
 * Country is optional: the admin form treats it as a nice-to-have, so requiring it here
 * would reject destinations the UI considers valid.
 */
public record DestinationRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must be at most 120 characters")
        String name,

        String slug,

        String country,

        String city,

        String description,

        String coverImageUrl,

        MediaRef image,

        Boolean featured,

        Double latitude,

        Double longitude,

        Boolean active
) {
}
