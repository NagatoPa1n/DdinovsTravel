package uz.nagato.touragency.tour.dto;

import uz.nagato.touragency.tour.entity.TourStatus;

import java.math.BigDecimal;

/** Query parameters accepted by the tour list endpoint. */
public record TourFilter(
        String search,
        Long categoryId,
        Long destinationId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minDuration,
        Integer maxDuration,
        Boolean featured,
        TourStatus status,
        boolean onlyActive
) {
}
