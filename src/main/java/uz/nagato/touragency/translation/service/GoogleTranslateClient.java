package uz.nagato.touragency.translation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uz.nagato.touragency.translation.TranslationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin wrapper over the Google Cloud Translation v2 REST API.
 * <p>
 * Never throws: a failed call returns an empty list and the caller serves the original
 * text. A translation outage must not take the website down.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleTranslateClient {

    private final TranslationProperties properties;

    private volatile RestClient client;

    private RestClient client() {
        RestClient existing = client;
        if (existing != null) {
            return existing;
        }
        synchronized (this) {
            if (client == null) {
                SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                factory.setConnectTimeout(Duration.ofMillis(properties.getTimeoutMs()));
                factory.setReadTimeout(Duration.ofMillis(properties.getTimeoutMs()));
                client = RestClient.builder().requestFactory(factory).build();
            }
            return client;
        }
    }

    /**
     * Translates a batch of strings.
     *
     * @return translations in the same order as {@code texts}, or an empty list on failure
     */
    public List<String> translate(List<String> texts, String targetLanguage) {
        if (texts.isEmpty()) {
            return List.of();
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("q", texts);
        body.put("target", targetLanguage);
        // The content holds markup, so tags must survive the round trip.
        body.put("format", "html");
        if (properties.getSourceLanguage() != null && !properties.getSourceLanguage().isBlank()) {
            body.put("source", properties.getSourceLanguage());
        }

        try {
            GoogleResponse response = client()
                    .post()
                    .uri(properties.getEndpoint() + "?key={key}", properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(GoogleResponse.class);

            if (response == null || response.data() == null || response.data().translations() == null) {
                log.warn("Translation API returned no data for {} string(s)", texts.size());
                return List.of();
            }

            List<String> translated = response.data().translations().stream()
                    .map(Translation::translatedText)
                    .map(HtmlEntities::unescape)
                    .toList();

            if (translated.size() != texts.size()) {
                log.warn("Translation API returned {} results for {} inputs; ignoring batch",
                        translated.size(), texts.size());
                return List.of();
            }
            return translated;

        } catch (Exception e) {
            // Bad key, quota exhausted, network down — serve the original text instead.
            log.warn("Translation request failed ({}); serving untranslated text", e.toString());
            return List.of();
        }
    }

    private record Translation(String translatedText) {
    }

    private record TranslationData(List<Translation> translations) {
    }

    private record GoogleResponse(TranslationData data) {
    }
}
