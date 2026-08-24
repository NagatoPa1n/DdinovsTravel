package uz.nagato.touragency.destination.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.nagato.touragency.common.dto.MediaRef;
import uz.nagato.touragency.common.entity.BaseEntity;
import uz.nagato.touragency.common.jpa.MediaRefConverter;

@Entity
@Table(name = "destinations")
@Getter
@Setter
@NoArgsConstructor
public class Destination extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String country;

    private String city;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    /** Media library item chosen in the admin form. */
    @Convert(converter = MediaRefConverter.class)
    @Column(name = "image", columnDefinition = "text")
    private MediaRef image;

    @Column(nullable = false)
    private boolean featured = false;

    private Double latitude;

    private Double longitude;

    @Column(nullable = false)
    private boolean active = true;
}
