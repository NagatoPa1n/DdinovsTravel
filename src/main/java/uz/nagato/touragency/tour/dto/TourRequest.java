package uz.nagato.touragency.tour.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TourRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 160, message = "Title must be at most 160 characters")
        String title,

        String slug,

        @Size(max = 500, message = "Short description must be at most 500 characters")
        String shortDescription,

        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
        BigDecimal price,

        @DecimalMin(value = "0.0", inclusive = false, message = "Discount price must be greater than zero")
        BigDecimal discountPrice,

        @NotNull(message = "Duration is required")
        @Min(value = 1, message = "Duration must be at least 1 day")
        Integer durationDays,

        @Min(value = 1, message = "Group size must be at least 1")
        Integer maxGroupSize,

        LocalDate startDate,

        LocalDate endDate,

        @NotNull(message = "Category is required")
        Long categoryId,

        @NotNull(message = "Destination is required")
        Long destinationId,

        String coverImageUrl,

        Boolean featured,

        Boolean active
) {
}
