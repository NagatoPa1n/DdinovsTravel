package uz.nagato.touragency.media.dto;

import java.util.List;

/** Body of {@code POST /api/media/bulk-delete}. */
public record BulkDeleteRequest(List<Long> ids) {
}
