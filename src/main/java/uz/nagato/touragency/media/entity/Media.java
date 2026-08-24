package uz.nagato.touragency.media.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.nagato.touragency.common.entity.BaseEntity;

/**
 * A stored file. Ownership is kept as a type plus id rather than a JPA relation so the media
 * module stays independent of the modules that use it.
 */
@Entity
@Table(name = "media", indexes = @Index(name = "idx_media_owner", columnList = "owner_type, owner_id"))
@Getter
@Setter
@NoArgsConstructor
public class Media extends BaseEntity {

    @Column(name = "file_name", nullable = false, unique = true)
    private String fileName;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size_bytes")
    private long sizeBytes;

    @Column(nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 20)
    private OwnerType ownerType = OwnerType.GENERAL;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "alt_text")
    private String altText;

    /** Human-facing label shown in the media library, independent of the stored file name. */
    @Column(name = "title")
    private String title;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "uploaded_by")
    private Long uploadedBy;
}
