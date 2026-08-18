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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.nagato.touragency.common.response.ApiResponse;
import uz.nagato.touragency.common.response.PageResponse;
import uz.nagato.touragency.tour.dto.TourFilter;
import uz.nagato.touragency.tour.dto.TourRequest;
import uz.nagato.touragency.tour.dto.TourResponse;
import uz.nagato.touragency.tour.service.TourService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

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
            @RequestParam(defaultValue = "true") boolean onlyActive,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        TourFilter filter = new TourFilter(search, categoryId, destinationId, minPrice, maxPrice,
                minDuration, maxDuration, featured, onlyActive);
        return ApiResponse.ok(tourService.search(filter, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<TourResponse> byId(@PathVariable Long id) {
        return ApiResponse.ok(tourService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<TourResponse> bySlug(@PathVariable String slug) {
        return ApiResponse.ok(tourService.findBySlug(slug));
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        tourService.delete(id);
        return ApiResponse.message("Tour deleted");
    }
}
