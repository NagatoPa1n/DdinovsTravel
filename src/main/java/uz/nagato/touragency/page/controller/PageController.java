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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.nagato.touragency.common.response.ApiResponse;
import uz.nagato.touragency.common.util.SlugUtils;
import uz.nagato.touragency.page.dto.PageDto;
import uz.nagato.touragency.page.dto.PageRequest;
import uz.nagato.touragency.page.service.PageService;
import uz.nagato.touragency.translation.RequestLanguage;
import uz.nagato.touragency.translation.service.ContentTranslator;
import uz.nagato.touragency.user.entity.Role;
import uz.nagato.touragency.user.entity.User;
import uz.nagato.touragency.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;
    private final UserService userService;
    private final RequestLanguage requestLanguage;
    private final ContentTranslator contentTranslator;

    /** Staff see drafts as well; anonymous callers only ever get published pages. */
    @GetMapping
    public ApiResponse<List<PageDto>> list(
            @RequestParam(required = false) String lang,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        List<PageDto> pages = pageService.findAll(!isStaff());
        return ApiResponse.ok(
                contentTranslator.translatePages(pages, requestLanguage.resolve(lang, acceptLanguage)));
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

    @GetMapping("/{slug}")
    public ApiResponse<PageDto> bySlug(
            @PathVariable String slug,
            @RequestParam(required = false) String lang,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        PageDto page = pageService.findBySlug(slug, !isStaff());
        return ApiResponse.ok(
                contentTranslator.translatePage(page, requestLanguage.resolve(lang, acceptLanguage)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<PageDto> create(@Valid @RequestBody PageRequest request) {
        return ApiResponse.ok("Page created", pageService.create(request));
    }

    /**
     * Addressable by numeric id or by slug. Saving by slug creates the page when it does
     * not exist yet, which is how the built-in pages (home, about, contact) come into being.
     */
    @PutMapping("/{key}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<PageDto> update(@PathVariable String key,
                                       @Valid @RequestBody PageRequest request) {
        Long id = asId(key);
        PageDto saved = id != null
                ? pageService.update(id, request)
                : pageService.saveBySlug(SlugUtils.slugify(key), request);
        return ApiResponse.ok("Page saved", saved);
    }

    @DeleteMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable String key) {
        Long id = asId(key);
        if (id != null) {
            pageService.delete(id);
        } else {
            pageService.deleteBySlug(SlugUtils.slugify(key));
        }
        return ApiResponse.message("Page deleted");
    }

    /** Null when the path segment is a slug rather than an id. */
    private Long asId(String key) {
        try {
            return Long.valueOf(key);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isStaff() {
        User current = userService.currentUser();
        return current != null && (current.getRole() == Role.ADMIN || current.getRole() == Role.MANAGER);
    }
}
