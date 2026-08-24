package uz.nagato.touragency.tour.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

/** Publication state of a tour. Serialised lowercase to match the admin UI's values. */
public enum TourStatus {

    DRAFT,
    PUBLISHED,
    ARCHIVED;

    @JsonValue
    public String jsonValue() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    @JsonCreator
    public static TourStatus from(String value) {
        if (value == null || value.isBlank()) {
            return DRAFT;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            return DRAFT;
        }
    }

    /** Only published tours are visible on the public site. */
    public boolean isPublic() {
        return this == PUBLISHED;
    }
}
