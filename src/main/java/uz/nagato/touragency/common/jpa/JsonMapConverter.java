package uz.nagato.touragency.common.jpa;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Free-form JSON object. Backs CMS page content and settings groups, whose keys are
 * defined by the admin UI rather than by the API.
 */
@Converter
public class JsonMapConverter extends JsonAttributeConverter<Map<String, Object>> {

    public JsonMapConverter() {
        super(new TypeReference<>() {
        });
    }

    @Override
    protected Map<String, Object> empty() {
        return new LinkedHashMap<>();
    }
}
