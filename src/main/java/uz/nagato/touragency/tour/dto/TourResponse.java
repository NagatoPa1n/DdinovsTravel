package uz.nagato.touragency.tour.dto;

import uz.nagato.touragency.category.dto.CategoryResponse;
import uz.nagato.touragency.destination.dto.DestinationResponse;
import uz.nagato.touragency.media.dto.MediaResponse;
import uz.nagato.touragency.tour.entity.Tour;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TourResponse(
        Long id,
        String title,
        String slug,
        String shortDescription,
        String description,
        BigDecimal price,
        BigDecimal discountPrice,
        int durationDays,
        Integer maxGroupSize,
        LocalDate startDate,
        LocalDate endDate,
        CategoryResponse category,
        DestinationResponse destination,
        String coverImageUrl,
        boolean featured,
        boolean active,
        List<MediaResponse> images
) {

    public static TourResponse from(Tour tour, List<MediaResponse> images) {
        return new TourResponse(
                tour.getId(),
                tour.getTitle(),
                tour.getSlug(),
                tour.getShortDescription(),
                tour.getDescription(),
                tour.getPrice(),
                tour.getDiscountPrice(),
                tour.getDurationDays(),
                tour.getMaxGroupSize(),
                tour.getStartDate(),
                tour.getEndDate(),
                CategoryResponse.from(tour.getCategory()),
                DestinationResponse.from(tour.getDestination()),
                tour.getCoverImageUrl(),
                tour.isFeatured(),
                tour.isActive(),
                images == null ? List.of() : images
        );
    }
}
