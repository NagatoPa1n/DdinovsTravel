package uz.nagato.touragency.destination.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.nagato.touragency.common.entity.BaseEntity;

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

    private Double latitude;

    private Double longitude;

    @Column(nullable = false)
    private boolean active = true;
}
