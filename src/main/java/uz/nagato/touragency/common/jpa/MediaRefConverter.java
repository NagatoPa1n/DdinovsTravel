package uz.nagato.touragency.common.jpa;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import uz.nagato.touragency.common.dto.MediaRef;

/** A single embedded media reference (cover image, destination image). */
@Converter
public class MediaRefConverter extends JsonAttributeConverter<MediaRef> {

    public MediaRefConverter() {
        super(new TypeReference<>() {
        });
    }

    @Override
    protected MediaRef empty() {
        return null;
    }
}
