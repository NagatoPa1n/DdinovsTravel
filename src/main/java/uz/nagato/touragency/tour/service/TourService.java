package uz.nagato.touragency.tour.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
import uz.nagato.touragency.tour.entity.Tour;
import uz.nagato.touragency.tour.repository.TourRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        tour.setShortDescription(request.shortDescription());
        tour.setDescription(request.description());
        tour.setPrice(request.price());
        tour.setDiscountPrice(request.discountPrice());
        tour.setDurationDays(request.durationDays());
        tour.setMaxGroupSize(request.maxGroupSize());
        tour.setStartDate(request.startDate());
        tour.setEndDate(request.endDate());
        tour.setCategory(categoryService.getEntity(request.categoryId()));
        tour.setDestination(destinationService.getEntity(request.destinationId()));
        tour.setCoverImageUrl(request.coverImageUrl());
        tour.setFeatured(request.featured() != null && request.featured());
        tour.setActive(request.active() == null || request.active());
    }

    /** Builds the dynamic where-clause; every filter field is optional. */
    private Specification<Tour> toSpecification(TourFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.onlyActive()) {
                predicates.add(cb.isTrue(root.get("active")));
            }
            if (filter.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.categoryId()));
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
                predicates.add(cb.greaterThanOrEqualTo(root.get("durationDays"), filter.minDuration()));
            }
            if (filter.maxDuration() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("durationDays"), filter.maxDuration()));
            }
            if (filter.featured() != null) {
                predicates.add(cb.equal(root.get("featured"), filter.featured()));
            }
            if (StringUtils.hasText(filter.search())) {
                String like = "%" + filter.search().trim().toLowerCase(Locale.ENGLISH) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("shortDescription"), "")), like),
                        cb.like(cb.lower(root.get("destination").get("name")), like),
                        cb.like(cb.lower(root.get("destination").get("country")), like)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
