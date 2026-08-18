package uz.nagato.touragency.page.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.nagato.touragency.common.response.ApiResponse;
import uz.nagato.touragency.page.dto.PageDto;
import uz.nagato.touragency.page.dto.PageRequest;
import uz.nagato.touragency.page.service.PageService;

import java.util.List;

@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    @GetMapping
    public ApiResponse<List<PageDto>> published() {
        return ApiResponse.ok(pageService.findAll(true));
    }

    @GetMapping("/{slug}")
    public ApiResponse<PageDto> bySlug(@PathVariable String slug) {
        return ApiResponse.ok(pageService.findBySlug(slug, true));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<List<PageDto>> all() {
        return ApiResponse.ok(pageService.findAll(false));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<PageDto> byId(@PathVariable Long id) {
        return ApiResponse.ok(pageService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<PageDto> create(@Valid @RequestBody PageRequest request) {
        return ApiResponse.ok("Page created", pageService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<PageDto> update(@PathVariable Long id,
                                       @Valid @RequestBody PageRequest request) {
        return ApiResponse.ok("Page updated", pageService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        pageService.delete(id);
        return ApiResponse.message("Page deleted");
    }
}
