package uz.nagato.touragency.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** Turns a human title into a URL-safe slug: "Samarqand Tour" becomes "samarqand-tour". */
public final class SlugUtils {

    private static final Pattern MARKS = Pattern.compile("\\p{M}");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern ILLEGAL = Pattern.compile("[^a-z0-9-]");
    private static final Pattern DASHES = Pattern.compile("-{2,}");
    private static final Pattern EDGES = Pattern.compile("^-+|-+$");

    private SlugUtils() {
    }

    public static String slugify(String input) {
        String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD);
        String slug = MARKS.matcher(normalized).replaceAll("");
        slug = WHITESPACE.matcher(slug).replaceAll("-").toLowerCase(Locale.ENGLISH);
        slug = ILLEGAL.matcher(slug).replaceAll("");
        slug = DASHES.matcher(slug).replaceAll("-");
        return EDGES.matcher(slug).replaceAll("");
    }

    /** Uses the given slug when present, otherwise derives one from the fallback source. */
    public static String slugOrDerive(String slug, String fallbackSource) {
        return (slug == null || slug.isBlank()) ? slugify(fallbackSource) : slugify(slug);
    }
}
