package uz.nagato.touragency.media.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import uz.nagato.touragency.common.exception.BadRequestException;
import uz.nagato.touragency.common.exception.NotFoundException;
import uz.nagato.touragency.common.response.PageResponse;
import uz.nagato.touragency.media.dto.MediaResponse;
import uz.nagato.touragency.media.dto.MediaUpdateRequest;
import uz.nagato.touragency.media.entity.Media;
import uz.nagato.touragency.media.entity.OwnerType;
import uz.nagato.touragency.media.repository.MediaRepository;
import uz.nagato.touragency.user.entity.User;
import uz.nagato.touragency.user.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Stores uploads on the local filesystem and tracks them in the media table. */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/svg+xml", "application/pdf",
            // The media library offers a video filter and renders video previews.
            "video/mp4", "video/webm", "video/quicktime", "video/x-msvideo");

    private final MediaRepository mediaRepository;
    private final UserService userService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.public-url}")
    private String publicUrl;

    private Path storageRoot;

    @PostConstruct
    void initStorage() {
        storageRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory: " + storageRoot, e);
        }
        log.info("Media storage directory: {}", storageRoot);
    }

    @Transactional
    public MediaResponse upload(MultipartFile file, OwnerType ownerType, Long ownerId, String altText) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ENGLISH))) {
            throw new BadRequestException("Unsupported file type: " + contentType);
        }

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalName);
        String storedName = UUID.randomUUID()
                + (extension == null ? "" : "." + extension.toLowerCase(Locale.ENGLISH));

        Path target = storageRoot.resolve(storedName).normalize();
        if (!target.startsWith(storageRoot)) {
            throw new BadRequestException("Invalid file name");
        }
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file " + originalName, e);
        }

        User current = userService.currentUser();
        Media media = new Media();
        media.setFileName(storedName);
        media.setOriginalName(originalName);
        media.setContentType(contentType);
        media.setSizeBytes(file.getSize());
        media.setUrl(publicUrl + "/" + storedName);
        media.setOwnerType(ownerType == null ? OwnerType.GENERAL : ownerType);
        media.setOwnerId(ownerId);
        media.setAltText(altText);
        media.setUploadedBy(current == null ? null : current.getId());
        return MediaResponse.from(mediaRepository.save(media));
    }

    public PageResponse<MediaResponse> list(OwnerType ownerType, Pageable pageable) {
        return list(ownerType, null, null, pageable);
    }

    /**
     * Library listing.
     *
     * @param type coarse bucket — {@code image}, {@code video} or {@code file}
     * @param search matched against the uploaded file name and title
     */
    public PageResponse<MediaResponse> list(OwnerType ownerType, String type, String search, Pageable pageable) {
        Specification<Media> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (ownerType != null) {
                predicates.add(cb.equal(root.get("ownerType"), ownerType));
            }
            if (StringUtils.hasText(type)) {
                predicates.add(contentTypePredicate(root, cb, type.trim().toLowerCase(Locale.ENGLISH)));
            }
            if (StringUtils.hasText(search)) {
                String like = "%" + search.trim().toLowerCase(Locale.ENGLISH) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("originalName")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("title"), "")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("altText"), "")), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return PageResponse.of(mediaRepository.findAll(spec, pageable).map(MediaResponse::from));
    }

    /** "file" means anything that is neither an image nor a video. */
    private Predicate contentTypePredicate(Root<Media> root, CriteriaBuilder cb, String type) {
        Expression<String> contentType = cb.lower(cb.coalesce(root.get("contentType"), ""));
        return switch (type) {
            case "image" -> cb.like(contentType, "image/%");
            case "video" -> cb.like(contentType, "video/%");
            case "file" -> cb.and(
                    cb.notLike(contentType, "image/%"),
                    cb.notLike(contentType, "video/%"));
            default -> cb.conjunction();
        };
    }

    public List<MediaResponse> findByOwner(OwnerType ownerType, Long ownerId) {
        return mediaRepository.findAllByOwnerTypeAndOwnerIdOrderBySortOrderAscIdAsc(ownerType, ownerId)
                .stream()
                .map(MediaResponse::from)
                .toList();
    }

    /** Batch variant so list endpoints avoid one media query per row. */
    public Map<Long, List<MediaResponse>> findByOwners(OwnerType ownerType, Collection<Long> ownerIds) {
        if (ownerIds == null || ownerIds.isEmpty()) {
            return Map.of();
        }
        return mediaRepository.findAllByOwnerTypeAndOwnerIdInOrderBySortOrderAscIdAsc(ownerType, ownerIds)
                .stream()
                .collect(Collectors.groupingBy(Media::getOwnerId,
                        Collectors.mapping(MediaResponse::from, Collectors.toList())));
    }

    @Transactional
    public MediaResponse update(Long id, MediaUpdateRequest request) {
        Media media = getEntity(id);
        if (request.ownerType() != null) {
            media.setOwnerType(request.ownerType());
            media.setOwnerId(request.ownerId());
        }
        if (request.resolvedAltText() != null) {
            media.setAltText(request.resolvedAltText());
        }
        if (request.title() != null) {
            media.setTitle(request.title());
        }
        if (request.sortOrder() != null) {
            media.setSortOrder(request.sortOrder());
        }
        return MediaResponse.from(mediaRepository.save(media));
    }

    /**
     * Deletes several files in one call. Missing ids are skipped rather than failing the
     * batch, so a stale selection in the library still clears the rows that do exist.
     *
     * @return how many rows were actually removed
     */
    @Transactional
    public int deleteAll(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        List<Media> found = mediaRepository.findAllById(ids);
        found.forEach(media -> {
            try {
                Files.deleteIfExists(storageRoot.resolve(media.getFileName()).normalize());
            } catch (IOException e) {
                log.warn("Could not delete file {} from disk", media.getFileName(), e);
            }
        });
        mediaRepository.deleteAll(found);
        return found.size();
    }

    @Transactional
    public void delete(Long id) {
        Media media = getEntity(id);
        try {
            Files.deleteIfExists(storageRoot.resolve(media.getFileName()).normalize());
        } catch (IOException e) {
            log.warn("Could not delete file {} from disk", media.getFileName(), e);
        }
        mediaRepository.delete(media);
    }

    /** A stored file together with the type it has to be served as. */
    public record StoredFile(Resource resource, MediaType contentType) {
    }

    /**
     * Loads a stored file for download, rejecting any path that escapes the storage root.
     *
     * <p>The content type travels with the bytes: responses carry
     * {@code X-Content-Type-Options: nosniff}, so a missing or wrong type is not something
     * the browser will look past — an image served as JSON simply does not render.
     */
    public StoredFile loadAsResource(String fileName) {
        Media media = mediaRepository.findByFileName(fileName)
                .orElseThrow(() -> new NotFoundException("File not found: " + fileName));
        Path target = storageRoot.resolve(media.getFileName()).normalize();
        if (!target.startsWith(storageRoot) || !Files.isReadable(target)) {
            throw new NotFoundException("File not found: " + fileName);
        }
        try {
            return new StoredFile(new UrlResource(target.toUri()), contentTypeOf(media, target));
        } catch (IOException e) {
            throw new NotFoundException("File not found: " + fileName);
        }
    }

    /** The type recorded at upload, else whatever the file itself says, else raw bytes. */
    private MediaType contentTypeOf(Media media, Path target) {
        MediaType recorded = parseOrNull(media.getContentType());
        if (recorded != null) {
            return recorded;
        }
        try {
            MediaType probed = parseOrNull(Files.probeContentType(target));
            if (probed != null) {
                return probed;
            }
        } catch (IOException e) {
            // Fall through to the generic type.
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    /** Never throws: a malformed stored type must not turn into a 500 on a public URL. */
    private MediaType parseOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return MediaType.parseMediaType(value);
        } catch (InvalidMediaTypeException e) {
            return null;
        }
    }

    public Media getEntity(Long id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Media", id));
    }

    /** Detaches every file attached to an owner that is being deleted. */
    @Transactional
    public void releaseOwner(OwnerType ownerType, Long ownerId) {
        List<Media> attached =
                mediaRepository.findAllByOwnerTypeAndOwnerIdOrderBySortOrderAscIdAsc(ownerType, ownerId);
        attached.forEach(media -> {
            media.setOwnerType(OwnerType.GENERAL);
            media.setOwnerId(null);
        });
        mediaRepository.saveAll(attached);
    }
}
