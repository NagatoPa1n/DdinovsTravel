package uz.nagato.touragency.contact.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uz.nagato.touragency.contact.TelegramProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thin wrapper over the Telegram Bot API's {@code sendMessage}.
 * <p>
 * Never throws: a failed send returns {@code false} and the caller decides what to tell
 * the visitor. Telegram being down must not surface as a stack trace on the website.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramClient {

    private final TelegramProperties properties;

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
     * Posts a message to the configured chat.
     *
     * @return whether Telegram accepted it
     */
    public boolean sendMessage(String text) {
        if (!properties.isConfigured()) {
            log.warn("Telegram is not configured; enquiry not delivered");
            return false;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("chat_id", properties.getChatId());
        body.put("text", text);
        body.put("parse_mode", "HTML");
        body.put("disable_web_page_preview", true);

        try {
            TelegramResponse response = client()
                    .post()
                    .uri(properties.getApiUrl() + "/bot{token}/sendMessage", properties.getBotToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(TelegramResponse.class);

            if (response == null || !response.ok()) {
                log.warn("Telegram rejected the enquiry: {}",
                        response == null ? "empty response" : response.description());
                return false;
            }
            return true;

        } catch (Exception e) {
            // Bad token, wrong chat id, bot never started, network down.
            log.warn("Telegram send failed ({})", e.toString());
            return false;
        }
    }

    private record TelegramResponse(boolean ok, String description) {
    }
}
