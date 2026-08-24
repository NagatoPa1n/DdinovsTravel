package uz.nagato.touragency.page.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.nagato.touragency.common.entity.BaseEntity;
import uz.nagato.touragency.common.jpa.JsonMapConverter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Editable content page such as "About us", "Terms" or "Contacts".
 * <p>
 * Two content styles are supported side by side: {@link #body} for pages that are simply
 * a block of HTML, and {@link #content} for pages assembled from named fields (the
 * homepage hero, featured section and call to action).
 */
@Entity
@Table(name = "pages")
@Getter
@Setter
@NoArgsConstructor
public class Page extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    /** Free-form HTML body. */
    @Column(name = "body", columnDefinition = "text")
    private String body;

    /** Structured field map for pages edited through a purpose-built form. */
    @Convert(converter = JsonMapConverter.class)
    @Column(name = "content", columnDefinition = "text")
    private Map<String, Object> content = new LinkedHashMap<>();

    @Column(length = 500)
    private String excerpt;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(nullable = false)
    private boolean published = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
}
