package uz.nagato.touragency.common.jpa;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;

import java.util.List;

/** JSON array of plain strings — used for a tour's included/excluded bullet lists. */
@Converter
public class StringListConverter extends JsonAttributeConverter<List<String>> {

    public StringListConverter() {
        super(new TypeReference<>() {
        });
    }

    @Override
    protected List<String> empty() {
        return List.of();
    }
}
