package uz.nagato.touragency.settings.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import uz.nagato.touragency.common.jpa.JsonMapConverter;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A named bag of site settings ("general", "contact", "social").
 * <p>
 * The keys inside each group are owned by the admin UI, so the payload is stored as
 * free-form JSON rather than as columns — adding a field to a settings screen needs
 * no schema change.
 */
@Entity
@Table(name = "settings")
@Getter
@Setter
@NoArgsConstructor
public class SettingGroup {

    @Id
    @Column(name = "group_name", length = 64)
    private String groupName;

    @Convert(converter = JsonMapConverter.class)
    @Column(name = "data", columnDefinition = "text")
    private Map<String, Object> data = new LinkedHashMap<>();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public SettingGroup(String groupName) {
        this.groupName = groupName;
    }
}
