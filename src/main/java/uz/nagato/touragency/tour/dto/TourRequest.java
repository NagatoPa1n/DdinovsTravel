package uz.nagato.touragency.tour.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uz.nagato.touragency.common.dto.MediaRef;
import uz.nagato.touragency.tour.entity.ItineraryDay;
import uz.nagato.touragency.tour.entity.TourStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Field names mirror the admin tour form so the client sends its state unchanged. */
public record TourRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 160, message = "Title must be at most 160 characters")
        String title,

        String slug,

        @Size(max = 500, message = "Excerpt must be at most 500 characters")
        String excerpt,

        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
        BigDecimal price,

        @DecimalMin(value = "0.0", inclusive = false, message = "Discount price must be greater than zero")
        BigDecimal discountPrice,

        @Size(max = 3, message = "Currency must be a 3-letter code")
        String currency,

        @NotNull(message = "Duration is required")
        @Min(value = 1, message = "A tour must last at least one day")
        Integer days,

        @Min(value = 0, message = "Nights cannot be negative")
        Integer nights,

        @Min(value = 1, message = "Group size must be at least 1")
        Integer groupSize,

        LocalDate startDate,

        LocalDate endDate,

        @NotNull(message = "Destination is required")
        Long destinationId,

        List<Long> categoryIds,

        MediaRef coverImage,

        List<MediaRef> gallery,

        List<ItineraryDay> itinerary,

        List<String> included,

        List<String> excluded,

        TourStatus status,

        Boolean featured,

        @Size(max = 160, message = "SEO title must be at most 160 characters")
        String seoTitle,

        @Size(max = 500, message = "SEO description must be at most 500 characters")
        String seoDescription
) {
}
