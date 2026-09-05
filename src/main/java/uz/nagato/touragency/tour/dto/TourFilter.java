package uz.nagato.touragency.tour.dto;

import uz.nagato.touragency.tour.entity.TourStatus;

import java.math.BigDecimal;

/** Query parameters accepted by the tour list endpoint. */
public record TourFilter(
        String search,
        Long categoryId,
        Long destinationId,

        /**
         * Slug equivalents of the two id filters above.
         *
         * The public site links destinations and categories by slug -- it never has the
         * database id to hand -- so the list endpoint accepts either. Sending neither
         * leaves the filter off.
         */
        String categorySlug,
        String destinationSlug,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minDuration,
        Integer maxDuration,
        Boolean featured,
        TourStatus status,
        boolean onlyActive
) {
}
