package uz.nagato.touragency.common.jpa;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import lombok.extern.slf4j.Slf4j;

/**
 * Stores structured columns as JSON text.
 * <p>
 * Text rather than a native {@code jsonb} column so the same mapping works on both
 * PostgreSQL (runtime) and H2 (the test profile) without a dialect-specific type.
 */
@Slf4j
public abstract class JsonAttributeConverter<T> implements AttributeConverter<T, String> {

    protected static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();

    private final TypeReference<T> type;

    protected JsonAttributeConverter(TypeReference<T> type) {
        this.type = type;
    }

    /** Value used when the column is null or unreadable, so callers never get null collections. */
    protected abstract T empty();

    @Override
    public String convertToDatabaseColumn(T attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not serialise value to JSON", e);
        }
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return empty();
        }
        try {
            T value = MAPPER.readValue(dbData, type);
            return value == null ? empty() : value;
        } catch (Exception e) {
            // A malformed row must not take the whole request down.
            log.warn("Ignoring unreadable JSON column value: {}", e.getMessage());
            return empty();
        }
    }
}
