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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.nagato.touragency.common.response.ApiResponse;
import uz.nagato.touragency.common.response.PageResponse;
import uz.nagato.touragency.destination.dto.DestinationRequest;
import uz.nagato.touragency.destination.dto.DestinationResponse;
import uz.nagato.touragency.destination.service.DestinationService;

@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    @GetMapping
    public ApiResponse<PageResponse<DestinationResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "true") boolean onlyActive,
            @PageableDefault(size = 12, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.ok(destinationService.search(search, onlyActive, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<DestinationResponse> byId(@PathVariable Long id) {
        return ApiResponse.ok(destinationService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<DestinationResponse> bySlug(@PathVariable String slug) {
        return ApiResponse.ok(destinationService.findBySlug(slug));
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
