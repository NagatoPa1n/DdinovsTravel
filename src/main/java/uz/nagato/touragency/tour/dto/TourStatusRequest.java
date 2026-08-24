package uz.nagato.touragency.tour.dto;

import jakarta.validation.constraints.NotNull;
import uz.nagato.touragency.tour.entity.TourStatus;

/** Body of {@code PATCH /api/tours/{id}/status}. */
public record TourStatusRequest(
        @NotNull(message = "Status is required")
        TourStatus status
) {
}
