package uz.nagato.touragency.destination.controller;

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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.nagato.touragency.common.response.ApiResponse;
import uz.nagato.touragency.common.response.PageResponse;
import uz.nagato.touragency.destination.dto.DestinationRequest;
import uz.nagato.touragency.destination.dto.DestinationResponse;
import uz.nagato.touragency.destination.service.DestinationService;
import uz.nagato.touragency.translation.RequestLanguage;
import uz.nagato.touragency.translation.service.ContentTranslator;

@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;
    private final RequestLanguage requestLanguage;
    private final ContentTranslator contentTranslator;

    @GetMapping
    public ApiResponse<PageResponse<DestinationResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "true") boolean onlyActive,
            @RequestParam(required = false) String lang,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            @PageableDefault(size = 12, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<DestinationResponse> page = destinationService.search(search, onlyActive, pageable);
        String language = requestLanguage.resolve(lang, acceptLanguage);
        if (language == null) {
            return ApiResponse.ok(page);
        }
        return ApiResponse.ok(new PageResponse<>(
                contentTranslator.translateDestinations(page.content(), language),
                page.page(), page.size(), page.totalElements(), page.totalPages(), page.last()));
    }

    @GetMapping("/{id}")
    public ApiResponse<DestinationResponse> byId(@PathVariable Long id) {
        return ApiResponse.ok(destinationService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<DestinationResponse> bySlug(
            @PathVariable String slug,
            @RequestParam(required = false) String lang,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        DestinationResponse destination = destinationService.findBySlug(slug);
        return ApiResponse.ok(contentTranslator.translateDestination(
                destination, requestLanguage.resolve(lang, acceptLanguage)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<DestinationResponse> create(@Valid @RequestBody DestinationRequest request) {
        return ApiResponse.ok("Destination created", destinationService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<DestinationResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody DestinationRequest request) {
        return ApiResponse.ok("Destination updated", destinationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        destinationService.delete(id);
        return ApiResponse.message("Destination deleted");
    }
}
