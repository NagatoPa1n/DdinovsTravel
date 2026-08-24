package uz.nagato.touragency.contact.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.nagato.touragency.common.exception.ServiceUnavailableException;
import uz.nagato.touragency.contact.dto.ContactRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

    private final TelegramClient telegram;

    /**
     * Delivers an enquiry to the agency's Telegram chat.
     *
     * <p>Nothing is stored, so a failed send means the enquiry is gone. It is reported to
     * the visitor rather than swallowed: someone who is told "sent" and never hears back
     * is worse off than someone told to phone instead.
     */
    public void submit(ContactRequest request) {
        if (!telegram.sendMessage(format(request))) {
            throw new ServiceUnavailableException(
                    "Your message could not be sent right now. Please call us instead.");
        }
    }

    /** Builds the Telegram message. Every field is escaped — the send uses HTML parse mode. */
    private String format(ContactRequest request) {
        StringBuilder text = new StringBuilder("<b>New enquiry</b>\n\n");
        text.append("<b>Name:</b> ").append(escape(request.name())).append('\n');
        text.append("<b>Phone:</b> ").append(escape(request.phone()));

        if (isPresent(request.email())) {
            text.append("\n<b>Email:</b> ").append(escape(request.email()));
        }
        if (isPresent(request.message())) {
            text.append("\n\n").append(escape(request.message().trim()));
        }
        return text.toString();
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Escapes the three characters Telegram's HTML mode treats as markup.
     * Without this a visitor typing "&lt;" would make the whole message fail to send.
     */
    private String escape(String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
