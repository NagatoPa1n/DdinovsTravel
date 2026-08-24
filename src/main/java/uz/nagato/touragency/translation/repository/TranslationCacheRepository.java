package uz.nagato.touragency.translation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.nagato.touragency.translation.entity.TranslationCache;

import java.util.Collection;
import java.util.List;

public interface TranslationCacheRepository extends JpaRepository<TranslationCache, Long> {

    List<TranslationCache> findAllByTargetLangAndSourceHashIn(String targetLang, Collection<String> sourceHashes);
}
