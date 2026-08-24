package uz.nagato.touragency.common.jpa;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import uz.nagato.touragency.common.dto.MediaRef;

import java.util.List;

/** Ordered gallery of embedded media references. */
@Converter
public class MediaRefListConverter extends JsonAttributeConverter<List<MediaRef>> {

    public MediaRefListConverter() {
        super(new TypeReference<>() {
        });
    }

    @Override
    protected List<MediaRef> empty() {
        return List.of();
    }
}
