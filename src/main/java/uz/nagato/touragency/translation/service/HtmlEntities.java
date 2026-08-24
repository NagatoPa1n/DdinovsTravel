package uz.nagato.touragency.translation.service;

/**
 * Google escapes a handful of characters in its response even when {@code format=html},
 * so quotes and ampersands come back as entities. This puts them back.
 */
final class HtmlEntities {

    private HtmlEntities() {
    }

    static String unescape(String value) {
        if (value == null || value.indexOf('&') < 0) {
            return value;
        }
        return value
                .replace("&#39;", "'")
                .replace("&#039;", "'")
                .replace("&apos;", "'")
                .replace("&quot;", "\"")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                // Ampersand last, so a decoded entity is not re-decoded.
                .replace("&amp;", "&");
    }
}
