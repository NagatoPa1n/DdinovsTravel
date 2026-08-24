package uz.nagato.touragency.tour.dto;

import uz.nagato.touragency.category.dto.CategoryResponse;
import uz.nagato.touragency.common.dto.MediaRef;
import uz.nagato.touragency.destination.dto.DestinationResponse;
import uz.nagato.touragency.media.dto.MediaResponse;
import uz.nagato.touragency.tour.entity.ItineraryDay;
import uz.nagato.touragency.tour.entity.Tour;
import uz.nagato.touragency.tour.entity.TourStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record TourResponse(
        Long id,
        String title,
        String slug,
        String excerpt,
        String description,
        BigDecimal price,
        BigDecimal discountPrice,
        String currency,
        int days,
        int nights,
        Integer groupSize,
        LocalDate startDate,
        LocalDate endDate,
        Long destinationId,
        DestinationResponse destination,
        List<Long> categoryIds,
        List<CategoryResponse> categories,
        CategoryResponse category,
        MediaRef coverImage,
        List<MediaRef> gallery,
        List<ItineraryDay> itinerary,
        List<String> included,
        List<String> excluded,
        TourStatus status,
        boolean featured,
        boolean active,
        String seoTitle,
        String seoDescription,
        List<MediaResponse> images,
        Instant createdAt,
        Instant updatedAt
) {

    public static TourResponse from(Tour tour, List<MediaResponse> images) {
        List<CategoryResponse> categories = tour.getCategories().stream()
                .map(CategoryResponse::from)
                .toList();

        return new TourResponse(
                tour.getId(),
                tour.getTitle(),
                tour.getSlug(),
                tour.getExcerpt(),
                tour.getDescription(),
                tour.getPrice(),
                tour.getDiscountPrice(),
                tour.getCurrency(),
                tour.getDays(),
                tour.getNights(),
                tour.getGroupSize(),
                tour.getStartDate(),
                tour.getEndDate(),
                tour.getDestination() == null ? null : tour.getDestination().getId(),
                DestinationResponse.from(tour.getDestination()),
                categories.stream().map(CategoryResponse::id).toList(),
                categories,
                CategoryResponse.from(tour.getCategory()),
                tour.getCoverImage(),
                tour.getGallery(),
                tour.getItinerary(),
                tour.getIncluded(),
                tour.getExcluded(),
                tour.getStatus(),
                tour.isFeatured(),
                tour.isActive(),
                tour.getSeoTitle(),
                tour.getSeoDescription(),
                images == null ? List.of() : images,
                tour.getCreatedAt(),
                tour.getUpdatedAt());
    }

    public static TourResponse from(Tour tour) {
        return from(tour, List.of());
    }
}
