package uz.nagato.touragency.translation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * One machine-translated string, kept so the same text is never paid for twice.
 * <p>
 * Keyed by a hash of the source text rather than by the record it came from, which means
 * editing a tour naturally misses the cache (the hash changes) without any explicit
 * invalidation, and identical strings across records share one entry.
 */
@Entity
@Table(
        name = "translation_cache",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_translation_source_lang",
                columnNames = {"source_hash", "target_lang"}),
        indexes = @Index(name = "idx_translation_lookup", columnList = "source_hash, target_lang"))
@Getter
@Setter
@NoArgsConstructor
public class TranslationCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SHA-256 of the source text, hex encoded. */
    @Column(name = "source_hash", nullable = false, length = 64)
    private String sourceHash;

    @Column(name = "target_lang", nullable = false, length = 8)
    private String targetLang;

    @Column(name = "translated", columnDefinition = "text")
    private String translated;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public TranslationCache(String sourceHash, String targetLang, String translated) {
        this.sourceHash = sourceHash;
        this.targetLang = targetLang;
        this.translated = translated;
    }
}
