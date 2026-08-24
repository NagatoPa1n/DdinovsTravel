package uz.nagato.touragency.contact;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Where contact-form enquiries are delivered. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.telegram")
public class TelegramProperties {

    /** Off unless a bot token and a chat id are both configured. */
    private boolean enabled = true;

    /** Token from @BotFather, in the form {@code 123456:ABC-DEF...}. */
    private String botToken = "";

    /** Chat the enquiry is posted to: a numeric user/group id, or {@code @channelname}. */
    private String chatId = "";

    private String apiUrl = "https://api.telegram.org";

    /** Milliseconds to wait on Telegram before giving up on the enquiry. */
    private int timeoutMs = 6000;

    public boolean isConfigured() {
        return enabled
                && botToken != null && !botToken.isBlank()
                && chatId != null && !chatId.isBlank();
    }
}
