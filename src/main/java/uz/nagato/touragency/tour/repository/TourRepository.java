package uz.nagato.touragency.tour.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import uz.nagato.touragency.tour.entity.Tour;

import java.util.Optional;

public interface TourRepository extends JpaRepository<Tour, Long>, JpaSpecificationExecutor<Tour> {

    @EntityGraph(attributePaths = {"category", "destination"})
    Optional<Tour> findBySlug(String slug);

    @EntityGraph(attributePaths = {"category", "destination"})
    Optional<Tour> findWithRelationsById(Long id);

    /** Category and destination are fetched eagerly here to keep list endpoints at one query. */
    @Override
    @EntityGraph(attributePaths = {"category", "destination"})
    Page<Tour> findAll(Specification<Tour> spec, Pageable pageable);

    /** Relabels every tour not already on {@code currency}; amounts are untouched. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Tour t set t.currency = :currency where t.currency <> :currency")
    int relabelCurrency(@Param("currency") String currency);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    long countByCategoryId(Long categoryId);

    long countByDestinationId(Long destinationId);
}
