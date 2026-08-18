package uz.nagato.touragency.page.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.nagato.touragency.common.entity.BaseEntity;

/** Editable content page such as "About us", "Terms" or "Contacts". */
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

    @Column(columnDefinition = "text")
    private String content;

    @Column(length = 500)
    private String excerpt;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(nullable = false)
    private boolean published = false;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
}
