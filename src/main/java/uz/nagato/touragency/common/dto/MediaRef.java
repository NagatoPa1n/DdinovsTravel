package uz.nagato.touragency.common.dto;

/**
 * Snapshot of a media library item embedded in another record (a tour cover image,
 * a destination image, a gallery entry).
 * <p>
 * Denormalised on purpose: the admin UI needs {@code url} to render a preview without a
 * second request, and deleting a file should not blank out the record that referenced it.
 */
public record MediaRef(
        Long id,
        String url,
        String thumbnailUrl,
        String filename,
        String alt,
        String type
) {
}
