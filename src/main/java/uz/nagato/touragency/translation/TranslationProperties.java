package uz.nagato.touragency.translation;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.translation")
public class TranslationProperties {

    /** Off unless an API key is configured; the site then serves original text. */
    private boolean enabled = false;

    /** Google Cloud Translation API key. */
    private String apiKey = "";

    private String endpoint = "https://translation.googleapis.com/language/translate/v2";

    /**
     * Language the content is authored in. Requests for this language skip translation
     * entirely, and it is sent to Google as the source hint.
     */
    private String sourceLanguage = "uz";

    /** Languages the API is willing to translate into. */
    private List<String> targetLanguages = List.of("en", "ru", "uz");

    /** Google rejects oversized requests; strings are sent in batches of this size. */
    private int batchSize = 64;

    /** Requests longer than this are returned untranslated rather than risking a huge bill. */
    private int maxCharsPerText = 12000;

    /** Milliseconds to wait on the Google call before giving up and serving the original. */
    private int timeoutMs = 6000;

    public boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    public boolean supports(String language) {
        return language != null && targetLanguages.contains(language);
    }
}
