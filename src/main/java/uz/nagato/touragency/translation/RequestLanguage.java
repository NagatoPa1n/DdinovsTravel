package uz.nagato.touragency.translation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.nagato.touragency.user.entity.Role;
import uz.nagato.touragency.user.entity.User;
import uz.nagato.touragency.user.service.UserService;

import java.util.Locale;

/**
 * Decides which language a request should be answered in.
 *
 * <p><strong>Staff never get translated content.</strong> The admin edit form loads a tour
 * and posts it straight back, so serving a machine translation there would overwrite the
 * author's original text with it on the next save. Editors always see what is stored.
 */
@Component
@RequiredArgsConstructor
public class RequestLanguage {

    private final UserService userService;
    private final TranslationProperties properties;

    /**
     * @param explicit       value of a {@code ?lang=} parameter, if any
     * @param acceptLanguage the {@code Accept-Language} header, if any
     * @return the language to translate into, or {@code null} to serve the text as authored
     */
    public String resolve(String explicit, String acceptLanguage) {
        if (isStaff()) {
            return null;
        }

        String language = normalise(explicit);
        if (language == null) {
            language = fromHeader(acceptLanguage);
        }
        if (language == null || language.equalsIgnoreCase(properties.getSourceLanguage())) {
            return null;
        }
        return properties.supports(language) ? language : null;
    }

    /** Reads the highest-priority tag we support out of an Accept-Language header. */
    private String fromHeader(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        for (String part : header.split(",")) {
            // "ru-RU;q=0.9" -> "ru"
            String tag = part.split(";")[0].trim();
            String language = normalise(tag);
            if (language != null && properties.supports(language)) {
                return language;
            }
        }
        return null;
    }

    private String normalise(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim().toLowerCase(Locale.ENGLISH);
        int dash = trimmed.indexOf('-');
        if (dash > 0) {
            trimmed = trimmed.substring(0, dash);
        }
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isStaff() {
        User current = userService.currentUser();
        return current != null && (current.getRole() == Role.ADMIN || current.getRole() == Role.MANAGER);
    }
}
