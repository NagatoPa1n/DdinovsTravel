package uz.nagato.touragency.tour.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.nagato.touragency.common.response.ApiResponse;
import uz.nagato.touragency.common.response.PageResponse;
import uz.nagato.touragency.tour.dto.TourFilter;
import uz.nagato.touragency.tour.dto.TourRequest;
import uz.nagato.touragency.tour.dto.TourResponse;
import uz.nagato.touragency.tour.dto.TourStatusRequest;
import uz.nagato.touragency.tour.entity.TourStatus;
import uz.nagato.touragency.tour.service.TourService;
import uz.nagato.touragency.translation.RequestLanguage;
import uz.nagato.touragency.translation.service.ContentTranslator;
import uz.nagato.touragency.user.entity.Role;
import uz.nagato.touragency.user.entity.User;
import uz.nagato.touragency.user.service.UserService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;
    private final UserService userService;
    private final RequestLanguage requestLanguage;
    private final ContentTranslator contentTranslator;

    @GetMapping
    public ApiResponse<PageResponse<TourResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long destinationId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) TourStatus status,
            @RequestParam(required = false) Boolean onlyActive,
            @RequestParam(required = false) String lang,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        TourFilter filter = new TourFilter(search, categoryId, destinationId, minPrice, maxPrice,
                minDuration, maxDuration, featured, status, resolveOnlyActive(onlyActive));

        PageResponse<TourResponse> page = tourService.search(filter, pageable);
        String language = requestLanguage.resolve(lang, acceptLanguage);
        if (language == null) {
            return ApiResponse.ok(page);
        }
        return ApiResponse.ok(new PageResponse<>(
                contentTranslator.translateTours(page.content(), language),
                page.page(), page.size(), page.totalElements(), page.totalPages(), page.last()));
    }

    /**
     * Always answers with the stored text — this is the endpoint the admin edit form loads
     * from, and a translated response here would be saved back over the original.
     */
    @GetMapping("/{id}")
    public ApiResponse<TourResponse> byId(@PathVariable Long id) {
        return ApiResponse.ok(tourService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<TourResponse> bySlug(
            @PathVariable String slug,
            @RequestParam(required = false) String lang,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        TourResponse tour = tourService.findBySlug(slug);
        return ApiResponse.ok(
                contentTranslator.translateTour(tour, requestLanguage.resolve(lang, acceptLanguage)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<TourResponse> create(@Valid @RequestBody TourRequest request) {
        return ApiResponse.ok("Tour created", tourService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<TourResponse> update(@PathVariable Long id,
                                            @Valid @RequestBody TourRequest request) {
        return ApiResponse.ok("Tour updated", tourService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<TourResponse> setStatus(@PathVariable Long id,
                                               @Valid @RequestBody TourStatusRequest request) {
        return ApiResponse.ok("Status updated", tourService.setStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        tourService.delete(id);
        return ApiResponse.message("Tour deleted");
    }

    /**
     * The admin catalogue needs drafts; the public site must never see them.
     * Staff may opt back into the published-only view with {@code ?onlyActive=true}.
     */
    private boolean resolveOnlyActive(Boolean requested) {
        if (!isStaff()) {
            return true;
        }
        return Boolean.TRUE.equals(requested);
    }

    private boolean isStaff() {
        User current = userService.currentUser();
        return current != null && (current.getRole() == Role.ADMIN || current.getRole() == Role.MANAGER);
    }
}
