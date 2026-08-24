package uz.nagato.touragency.translation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.nagato.touragency.category.dto.CategoryResponse;
import uz.nagato.touragency.destination.dto.DestinationResponse;
import uz.nagato.touragency.page.dto.PageDto;
import uz.nagato.touragency.tour.dto.TourResponse;
import uz.nagato.touragency.tour.entity.ItineraryDay;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Swaps the human-readable fields of a response for their translations.
 * <p>
 * Every string on a page is collected first and translated in one batched call, so a
 * list of tours costs one request rather than one per tour. Identifiers, slugs, URLs,
 * prices and dates are deliberately left alone — translating a slug would break links.
 */
@Service
@RequiredArgsConstructor
public class ContentTranslator {

    private final TranslationService translationService;

    public boolean isActive(String language) {
        return translationService.isActive(language);
    }

    // ---------------------------------------------------------------- tours

    public List<TourResponse> translateTours(List<TourResponse> tours, String language) {
        if (!isActive(language) || tours == null || tours.isEmpty()) {
            return tours;
        }
        Set<String> texts = new LinkedHashSet<>();
        tours.forEach(tour -> collectTour(tour, texts));

        Map<String, String> dictionary = translationService.translate(texts, language);
        if (dictionary.isEmpty()) {
            return tours;
        }
        return tours.stream().map(tour -> applyTour(tour, dictionary)).toList();
    }

    public TourResponse translateTour(TourResponse tour, String language) {
        if (!isActive(language) || tour == null) {
            return tour;
        }
        return translateTours(List.of(tour), language).get(0);
    }

    private void collectTour(TourResponse tour, Set<String> texts) {
        add(texts, tour.title(), tour.excerpt(), tour.description(),
                tour.seoTitle(), tour.seoDescription());
        if (tour.itinerary() != null) {
            tour.itinerary().forEach(day -> add(texts, day.title(), day.description()));
        }
        if (tour.included() != null) tour.included().forEach(item -> add(texts, item));
        if (tour.excluded() != null) tour.excluded().forEach(item -> add(texts, item));
        collectDestination(tour.destination(), texts);
        if (tour.categories() != null) tour.categories().forEach(c -> collectCategory(c, texts));
        collectCategory(tour.category(), texts);
    }

    private TourResponse applyTour(TourResponse tour, Map<String, String> dictionary) {
        List<ItineraryDay> itinerary = tour.itinerary() == null ? null : tour.itinerary().stream()
                .map(day -> new ItineraryDay(
                        day.day(),
                        pick(dictionary, day.title()),
                        pick(dictionary, day.description())))
                .toList();

        return new TourResponse(
                tour.id(),
                pick(dictionary, tour.title()),
                tour.slug(),
                pick(dictionary, tour.excerpt()),
                pick(dictionary, tour.description()),
                tour.price(),
                tour.discountPrice(),
                tour.currency(),
                tour.days(),
                tour.nights(),
                tour.groupSize(),
                tour.startDate(),
                tour.endDate(),
                tour.destinationId(),
                applyDestination(tour.destination(), dictionary),
                tour.categoryIds(),
                tour.categories() == null ? null
                        : tour.categories().stream().map(c -> applyCategory(c, dictionary)).toList(),
                applyCategory(tour.category(), dictionary),
                tour.coverImage(),
                tour.gallery(),
                itinerary,
                pickAll(dictionary, tour.included()),
                pickAll(dictionary, tour.excluded()),
                tour.status(),
                tour.featured(),
                tour.active(),
                pick(dictionary, tour.seoTitle()),
                pick(dictionary, tour.seoDescription()),
                tour.images(),
                tour.createdAt(),
                tour.updatedAt());
    }

    // --------------------------------------------------------- destinations

    public List<DestinationResponse> translateDestinations(List<DestinationResponse> destinations, String language) {
        if (!isActive(language) || destinations == null || destinations.isEmpty()) {
            return destinations;
        }
        Set<String> texts = new LinkedHashSet<>();
        destinations.forEach(destination -> collectDestination(destination, texts));

        Map<String, String> dictionary = translationService.translate(texts, language);
        if (dictionary.isEmpty()) {
            return destinations;
        }
        return destinations.stream().map(d -> applyDestination(d, dictionary)).toList();
    }

    public DestinationResponse translateDestination(DestinationResponse destination, String language) {
        if (!isActive(language) || destination == null) {
            return destination;
        }
        return translateDestinations(List.of(destination), language).get(0);
    }

    private void collectDestination(DestinationResponse destination, Set<String> texts) {
        if (destination == null) return;
        add(texts, destination.name(), destination.country(), destination.city(), destination.description());
    }

    private DestinationResponse applyDestination(DestinationResponse destination, Map<String, String> dictionary) {
        if (destination == null) return null;
        return new DestinationResponse(
                destination.id(),
                pick(dictionary, destination.name()),
                destination.slug(),
                pick(dictionary, destination.country()),
                pick(dictionary, destination.city()),
                pick(dictionary, destination.description()),
                destination.coverImageUrl(),
                destination.image(),
                destination.featured(),
                destination.latitude(),
                destination.longitude(),
                destination.active(),
                destination.createdAt(),
                destination.updatedAt());
    }

    // ----------------------------------------------------------- categories

    public List<CategoryResponse> translateCategories(List<CategoryResponse> categories, String language) {
        if (!isActive(language) || categories == null || categories.isEmpty()) {
            return categories;
        }
        Set<String> texts = new LinkedHashSet<>();
        categories.forEach(category -> collectCategory(category, texts));

        Map<String, String> dictionary = translationService.translate(texts, language);
        if (dictionary.isEmpty()) {
            return categories;
        }
        return categories.stream().map(c -> applyCategory(c, dictionary)).toList();
    }

    public CategoryResponse translateCategory(CategoryResponse category, String language) {
        if (!isActive(language) || category == null) {
            return category;
        }
        return translateCategories(List.of(category), language).get(0);
    }

    private void collectCategory(CategoryResponse category, Set<String> texts) {
        if (category == null) return;
        add(texts, category.name(), category.description());
    }

    private CategoryResponse applyCategory(CategoryResponse category, Map<String, String> dictionary) {
        if (category == null) return null;
        return new CategoryResponse(
                category.id(),
                pick(dictionary, category.name()),
                category.slug(),
                pick(dictionary, category.description()),
                category.iconUrl(),
                category.active());
    }

    // ---------------------------------------------------------------- pages

    public List<PageDto> translatePages(List<PageDto> pages, String language) {
        if (!isActive(language) || pages == null || pages.isEmpty()) {
            return pages;
        }
        Set<String> texts = new LinkedHashSet<>();
        pages.forEach(page -> collectPage(page, texts));

        Map<String, String> dictionary = translationService.translate(texts, language);
        if (dictionary.isEmpty()) {
            return pages;
        }
        return pages.stream().map(page -> applyPage(page, dictionary)).toList();
    }

    public PageDto translatePage(PageDto page, String language) {
        if (!isActive(language) || page == null) {
            return page;
        }
        return translatePages(List.of(page), language).get(0);
    }

    private void collectPage(PageDto page, Set<String> texts) {
        add(texts, page.title(), page.body(), page.excerpt(), page.seoTitle(), page.seoDescription());
        if (page.content() != null) {
            page.content().values().forEach(value -> {
                if (value instanceof String text) add(texts, text);
            });
        }
    }

    private PageDto applyPage(PageDto page, Map<String, String> dictionary) {
        Map<String, Object> content = null;
        if (page.content() != null) {
            content = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : page.content().entrySet()) {
                Object value = entry.getValue();
                content.put(entry.getKey(),
                        value instanceof String text ? pick(dictionary, text) : value);
            }
        }

        return new PageDto(
                page.id(),
                page.slug(),
                pick(dictionary, page.title()),
                pick(dictionary, page.body()),
                content,
                pick(dictionary, page.excerpt()),
                pick(dictionary, page.seoTitle()),
                pick(dictionary, page.seoDescription()),
                pick(dictionary, page.metaTitle()),
                pick(dictionary, page.metaDescription()),
                page.status(),
                page.published(),
                page.sortOrder(),
                page.createdAt(),
                page.updatedAt());
    }

    // --------------------------------------------------------------- shared

    private void add(Set<String> texts, String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) texts.add(value);
        }
    }

    /** Translation when there is one, otherwise the original. */
    private String pick(Map<String, String> dictionary, String value) {
        if (value == null) return null;
        return dictionary.getOrDefault(value, value);
    }

    private List<String> pickAll(Map<String, String> dictionary, List<String> values) {
        if (values == null) return null;
        List<String> out = new ArrayList<>(values.size());
        values.forEach(value -> out.add(pick(dictionary, value)));
        return out;
    }
}
