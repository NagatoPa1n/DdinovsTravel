package uz.nagato.touragency.translation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.nagato.touragency.translation.TranslationProperties;
import uz.nagato.touragency.translation.entity.TranslationCache;
import uz.nagato.touragency.translation.repository.TranslationCacheRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Translates strings through Google, caching every result in the database.
 * <p>
 * The cache is the important part: page views are far more frequent than content edits,
 * so without it every visitor would be billed for the same paragraphs over and over.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final TranslationProperties properties;
    private final GoogleTranslateClient client;
    private final TranslationCacheRepository cacheRepository;

    public boolean isActive(String targetLanguage) {
        return properties.isConfigured()
                && properties.supports(targetLanguage)
                && !properties.getSourceLanguage().equalsIgnoreCase(targetLanguage);
    }

    /**
     * Translates a set of distinct strings.
     *
     * @return source text mapped to its translation; sources that could not be translated
     *         are absent, so callers should fall back to the original
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<String, String> translate(Collection<String> texts, String targetLanguage) {
        if (!isActive(targetLanguage) || texts == null || texts.isEmpty()) {
            return Map.of();
        }

        Set<String> distinct = new LinkedHashSet<>();
        for (String text : texts) {
            if (isTranslatable(text)) distinct.add(text);
        }
        if (distinct.isEmpty()) {
            return Map.of();
        }

        String language = targetLanguage.toLowerCase(Locale.ENGLISH);
        Map<String, String> hashBySource = new HashMap<>();
        distinct.forEach(text -> hashBySource.put(text, sha256(text)));

        Map<String, String> result = new HashMap<>();

        // 1. Anything already paid for.
        List<TranslationCache> cached =
                cacheRepository.findAllByTargetLangAndSourceHashIn(language, hashBySource.values());
        Map<String, String> byHash = new HashMap<>();
        cached.forEach(entry -> byHash.put(entry.getSourceHash(), entry.getTranslated()));

        List<String> misses = new ArrayList<>();
        for (String text : distinct) {
            String hit = byHash.get(hashBySource.get(text));
            if (hit != null) {
                result.put(text, hit);
            } else {
                misses.add(text);
            }
        }
        if (misses.isEmpty()) {
            return result;
        }

        // 2. Everything else, in batches.
        Set<String> stored = new HashSet<>();
        for (int from = 0; from < misses.size(); from += properties.getBatchSize()) {
            List<String> batch = misses.subList(from, Math.min(from + properties.getBatchSize(), misses.size()));
            List<String> translated = client.translate(batch, language);
            if (translated.isEmpty()) {
                // The call failed; leave these untranslated rather than caching a bad result.
                continue;
            }

            List<TranslationCache> toSave = new ArrayList<>(batch.size());
            for (int i = 0; i < batch.size(); i++) {
                String source = batch.get(i);
                String value = translated.get(i);
                if (value == null) continue;

                result.put(source, value);
                String hash = hashBySource.get(source);
                // Guard against a duplicate inside the same batch tripping the unique index.
                if (stored.add(hash)) {
                    toSave.add(new TranslationCache(hash, language, value));
                }
            }
            persist(toSave);
        }

        return result;
    }

    /** Convenience for a single string. */
    public String translateOne(String text, String targetLanguage) {
        if (!isTranslatable(text)) return text;
        return translate(List.of(text), targetLanguage).getOrDefault(text, text);
    }

    private void persist(List<TranslationCache> entries) {
        if (entries.isEmpty()) return;
        try {
            cacheRepository.saveAll(entries);
        } catch (Exception e) {
            // A cache write racing another request is not worth failing the response over.
            log.debug("Could not cache {} translation(s): {}", entries.size(), e.getMessage());
        }
    }

    private boolean isTranslatable(String text) {
        return text != null
                && !text.isBlank()
                && text.length() <= properties.getMaxCharsPerText();
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
