package uz.nagato.touragency.page.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.nagato.touragency.page.entity.Page;

import java.util.List;
import java.util.Optional;

public interface PageRepository extends JpaRepository<Page, Long> {

    Optional<Page> findBySlug(String slug);

    Optional<Page> findBySlugAndPublishedTrue(String slug);

    List<Page> findAllByPublishedTrueOrderBySortOrderAscTitleAsc();

    List<Page> findAllByOrderBySortOrderAscTitleAsc();

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
