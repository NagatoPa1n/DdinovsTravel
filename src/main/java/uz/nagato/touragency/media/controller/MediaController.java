package uz.nagato.touragency.media.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uz.nagato.touragency.common.response.ApiResponse;
import uz.nagato.touragency.common.response.PageResponse;
import uz.nagato.touragency.media.dto.BulkDeleteRequest;
import uz.nagato.touragency.media.dto.MediaResponse;
import uz.nagato.touragency.media.dto.MediaUpdateRequest;
import uz.nagato.touragency.media.entity.OwnerType;
import uz.nagato.touragency.media.service.MediaService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    /**
     * {@code /api/media} and {@code /api/media/upload} are the same endpoint; the second
     * path exists because the admin uploader posts there.
     */
    @PostMapping(path = {"", "/upload"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<MediaResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) OwnerType ownerType,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String altText) {
        return ApiResponse.ok("File uploaded", mediaService.upload(file, ownerType, ownerId, altText));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<PageResponse<MediaResponse>> list(
            @RequestParam(required = false) OwnerType ownerType,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 24, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(mediaService.list(ownerType, type, search, pageable));
    }

    @GetMapping("/owner/{ownerType}/{ownerId}")
    public ApiResponse<List<MediaResponse>> byOwner(@PathVariable OwnerType ownerType,
                                                    @PathVariable Long ownerId) {
        return ApiResponse.ok(mediaService.findByOwner(ownerType, ownerId));
    }

    /** Public file download endpoint; the stored file name is the only accepted identifier. */
    @GetMapping("/files/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable String fileName) {
        MediaService.StoredFile file = mediaService.loadAsResource(fileName);
        return ResponseEntity.ok()
                .contentType(file.contentType())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.resource().getFilename() + "\"")
                .body(file.resource());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<MediaResponse> update(@PathVariable Long id,
                                             @RequestBody MediaUpdateRequest request) {
        return ApiResponse.ok("Media updated", mediaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        mediaService.delete(id);
        return ApiResponse.message("Media deleted");
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Map<String, Integer>> bulkDelete(@RequestBody BulkDeleteRequest request) {
        int removed = mediaService.deleteAll(request.ids());
        return ApiResponse.ok("Files deleted", Map.of("deleted", removed));
    }
}
