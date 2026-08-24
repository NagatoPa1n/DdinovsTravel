package uz.nagato.touragency.tour.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.nagato.touragency.category.entity.Category;
import uz.nagato.touragency.common.dto.MediaRef;
import uz.nagato.touragency.common.entity.BaseEntity;
import uz.nagato.touragency.common.jpa.ItineraryConverter;
import uz.nagato.touragency.common.jpa.MediaRefConverter;
import uz.nagato.touragency.common.jpa.MediaRefListConverter;
import uz.nagato.touragency.common.jpa.StringListConverter;
import uz.nagato.touragency.destination.entity.Destination;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tours")
@Getter
@Setter
@NoArgsConstructor
public class Tour extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    /** Card/teaser copy. */
    @Column(name = "excerpt", length = 500)
    private String excerpt;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** Sale price shown instead of {@link #price} when set. */
    @Column(name = "discount_price", precision = 12, scale = 2)
    private BigDecimal discountPrice;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "days", nullable = false)
    private int days;

    @Column(name = "nights", nullable = false)
    private int nights;

    @Column(name = "group_size")
    private Integer groupSize;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Primary category, kept as a single column so listings can sort and group cheaply.
     * Mirrors the first entry of {@link #categories}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "tour_categories",
            joinColumns = @JoinColumn(name = "tour_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new LinkedHashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    @Convert(converter = MediaRefConverter.class)
    @Column(name = "cover_image", columnDefinition = "text")
    private MediaRef coverImage;

    @Convert(converter = MediaRefListConverter.class)
    @Column(name = "gallery", columnDefinition = "text")
    private List<MediaRef> gallery = new ArrayList<>();

    @Convert(converter = ItineraryConverter.class)
    @Column(name = "itinerary", columnDefinition = "text")
    private List<ItineraryDay> itinerary = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(name = "included", columnDefinition = "text")
    private List<String> included = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(name = "excluded", columnDefinition = "text")
    private List<String> excluded = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TourStatus status = TourStatus.DRAFT;

    @Column(nullable = false)
    private boolean featured = false;

    /**
     * Derived from {@link #status}; kept as a column so the public site can filter on a
     * plain boolean. Always set through {@link #setStatus(TourStatus)}.
     */
    @Column(nullable = false)
    private boolean active = false;

    @Column(name = "seo_title")
    private String seoTitle;

    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    public void setStatus(TourStatus status) {
        this.status = status == null ? TourStatus.DRAFT : status;
        this.active = this.status.isPublic();
    }
}
