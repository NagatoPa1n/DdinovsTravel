package uz.nagato.touragency.tour.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.nagato.touragency.category.entity.Category;
import uz.nagato.touragency.category.service.CategoryService;
import uz.nagato.touragency.common.exception.BadRequestException;
import uz.nagato.touragency.common.exception.ConflictException;
import uz.nagato.touragency.common.exception.NotFoundException;
import uz.nagato.touragency.common.response.PageResponse;
import uz.nagato.touragency.common.util.SlugUtils;
import uz.nagato.touragency.destination.service.DestinationService;
import uz.nagato.touragency.media.dto.MediaResponse;
import uz.nagato.touragency.media.entity.OwnerType;
import uz.nagato.touragency.media.service.MediaService;
import uz.nagato.touragency.tour.dto.TourFilter;
import uz.nagato.touragency.tour.dto.TourRequest;
import uz.nagato.touragency.tour.dto.TourResponse;
import uz.nagato.touragency.tour.entity.ItineraryDay;
import uz.nagato.touragency.tour.entity.Tour;
import uz.nagato.touragency.tour.entity.TourStatus;
import uz.nagato.touragency.tour.repository.TourRepository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourService {

    private final TourRepository tourRepository;
    private final CategoryService categoryService;
    private final DestinationService destinationService;
    private final MediaService mediaService;

    public PageResponse<TourResponse> search(TourFilter filter, Pageable pageable) {
        Page<Tour> page = tourRepository.findAll(toSpecification(filter), pageable);
        List<Long> ids = page.getContent().stream().map(Tour::getId).toList();
        Map<Long, List<MediaResponse>> imagesByTour = mediaService.findByOwners(OwnerType.TOUR, ids);
        return PageResponse.of(page.map(tour ->
                TourResponse.from(tour, imagesByTour.getOrDefault(tour.getId(), List.of()))));
    }

    public TourResponse findById(Long id) {
        Tour tour = tourRepository.findWithRelationsById(id)
                .orElseThrow(() -> NotFoundException.of("Tour", id));
        return withImages(tour);
    }

    public TourResponse findBySlug(String slug) {
        Tour tour = tourRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Tour not found with slug: " + slug));
        return withImages(tour);
    }

    @Transactional
    public TourResponse create(TourRequest request) {
        String slug = SlugUtils.slugOrDerive(request.slug(), request.title());
        if (tourRepository.existsBySlug(slug)) {
            throw new ConflictException("Tour slug already in use: " + slug);
        }
        Tour tour = new Tour();
        apply(tour, request, slug);
        return withImages(tourRepository.save(tour));
    }

    @Transactional
    public TourResponse update(Long id, TourRequest request) {
        Tour tour = getEntity(id);
        String slug = SlugUtils.slugOrDerive(request.slug(), request.title());
        if (tourRepository.existsBySlugAndIdNot(slug, id)) {
            throw new ConflictException("Tour slug already in use: " + slug);
        }
        apply(tour, request, slug);
        return withImages(tourRepository.save(tour));
    }

    /** Publish or unpublish without round-tripping the whole tour. */
    @Transactional
    public TourResponse setStatus(Long id, TourStatus status) {
        Tour tour = getEntity(id);
        tour.setStatus(status);
        return withImages(tourRepository.save(tour));
    }

    @Transactional
    public void delete(Long id) {
        Tour tour = getEntity(id);
        mediaService.releaseOwner(OwnerType.TOUR, tour.getId());
        tourRepository.delete(tour);
    }

    public Tour getEntity(Long id) {
        return tourRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Tour", id));
    }

    private TourResponse withImages(Tour tour) {
        return TourResponse.from(tour, mediaService.findByOwner(OwnerType.TOUR, tour.getId()));
    }

    private void apply(Tour tour, TourRequest request, String slug) {
        if (request.startDate() != null && request.endDate() != null
                && request.endDate().isBefore(request.startDate())) {
            throw new BadRequestException("End date must not be before start date");
        }
        if (request.discountPrice() != null && request.discountPrice().compareTo(request.price()) >= 0) {
            throw new BadRequestException("Discount price must be lower than the regular price");
        }

        tour.setTitle(request.title());
        tour.setSlug(slug);
        tour.setExcerpt(request.excerpt());
        tour.setDescription(request.description());
        tour.setPrice(request.price());
        tour.setDiscountPrice(request.discountPrice());
        tour.setCurrency(StringUtils.hasText(request.currency())
                ? request.currency().trim().toUpperCase(Locale.ENGLISH)
                : "UZS");
        tour.setDays(request.days());
        // A trip is normally one night shorter than its day count unless stated otherwise.
        tour.setNights(request.nights() != null ? request.nights() : Math.max(0, request.days() - 1));
        tour.setGroupSize(request.groupSize());
        tour.setStartDate(request.startDate());
        tour.setEndDate(request.endDate());
        tour.setDestination(destinationService.getEntity(request.destinationId()));

        applyCategories(tour, request.categoryIds());

        tour.setCoverImage(request.coverImage());
        tour.setGallery(nullSafe(request.gallery()));
        tour.setItinerary(renumber(request.itinerary()));
        tour.setIncluded(nullSafe(request.included()));
        tour.setExcluded(nullSafe(request.excluded()));
        tour.setStatus(request.status());
        tour.setFeatured(Boolean.TRUE.equals(request.featured()));
        tour.setSeoTitle(request.seoTitle());
        tour.setSeoDescription(request.seoDescription());
    }

    /** Keeps the single {@code category} column in step with the set: the first id wins. */
    private void applyCategories(Tour tour, List<Long> categoryIds) {
        Set<Category> resolved = new LinkedHashSet<>();
        if (categoryIds != null) {
            categoryIds.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(categoryService::getEntity)
                    .forEach(resolved::add);
        }
        tour.setCategories(resolved);
        tour.setCategory(resolved.isEmpty() ? null : resolved.iterator().next());
    }

    /** Days are renumbered on save so gaps left by client-side reordering never persist. */
    private List<ItineraryDay> renumber(List<ItineraryDay> itinerary) {
        if (itinerary == null || itinerary.isEmpty()) {
            return new ArrayList<>();
        }
        List<ItineraryDay> ordered = new ArrayList<>(itinerary.size());
        for (int i = 0; i < itinerary.size(); i++) {
            ItineraryDay day = itinerary.get(i);
            ordered.add(new ItineraryDay(i + 1, day.title(), day.description()));
        }
        return ordered;
    }

    private <T> List<T> nullSafe(List<T> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }

    /** Builds the dynamic where-clause; every filter field is optional. */
    private Specification<Tour> toSpecification(TourFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.onlyActive()) {
                predicates.add(cb.equal(root.get("status"), TourStatus.PUBLISHED));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.categoryId() != null) {
                // Matches the many-to-many set, not only the primary category.
                predicates.add(cb.equal(root.join("categories").get("id"), filter.categoryId()));
                if (query != null) {
                    query.distinct(true);
                }
            }
            if (filter.destinationId() != null) {
                predicates.add(cb.equal(root.get("destination").get("id"), filter.destinationId()));
            }
            if (filter.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
            }
            if (filter.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
            }
            if (filter.minDuration() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("days"), filter.minDuration()));
            }
            if (filter.maxDuration() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("days"), filter.maxDuration()));
            }
            if (filter.featured() != null) {
                predicates.add(cb.equal(root.get("featured"), filter.featured()));
            }
            if (StringUtils.hasText(filter.search())) {
                String like = "%" + filter.search().trim().toLowerCase(Locale.ENGLISH) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("excerpt"), "")), like),
                        cb.like(cb.lower(root.get("destination").get("name")), like),
                        cb.like(cb.lower(root.get("destination").get("country")), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
