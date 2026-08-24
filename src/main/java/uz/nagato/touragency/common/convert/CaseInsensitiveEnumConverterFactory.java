package uz.nagato.touragency.common.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.lang.Nullable;

import java.util.Locale;

/**
 * Converts query-string values to enums without caring about case.
 * <p>
 * Request bodies go through Jackson, so {@code @JsonCreator} handles the lowercase values the
 * admin UI sends (for example {@code "published"}). Query parameters do not: they are bound by
 * Spring's own {@code ConversionService}, whose default enum converter calls
 * {@link Enum#valueOf} and rejects anything that is not an exact uppercase match. That mismatch
 * made {@code ?status=published} fail while {@code {"status":"published"}} worked.
 * <p>
 * A blank value converts to {@code null} rather than an error, so {@code ?status=} reads as
 * "no filter" instead of accidentally selecting the first constant.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CaseInsensitiveEnumConverterFactory implements ConverterFactory<String, Enum> {

    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnum<>(resolveEnumType(targetType));
    }

    /** Constants with a body are anonymous subclasses, so walk up to the declaring enum. */
    private static Class<?> resolveEnumType(Class<?> targetType) {
        Class<?> enumType = targetType;
        while (enumType != null && !enumType.isEnum()) {
            enumType = enumType.getSuperclass();
        }
        if (enumType == null) {
            throw new IllegalArgumentException(targetType.getName() + " is not an enum");
        }
        return enumType;
    }

    private record StringToEnum<T extends Enum>(Class<?> enumType) implements Converter<String, T> {

        @Override
        @Nullable
        public T convert(String source) {
            String value = source.trim();
            if (value.isEmpty()) {
                return null;
            }
            for (Object constant : enumType.getEnumConstants()) {
                if (((Enum<?>) constant).name().equalsIgnoreCase(value)) {
                    return (T) constant;
                }
            }
            throw new IllegalArgumentException("No enum constant %s.%s"
                    .formatted(enumType.getSimpleName(), value.toUpperCase(Locale.ENGLISH)));
        }
    }
}
