package uz.nagato.touragency.tour.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
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

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    long countByCategoryId(Long categoryId);

    long countByDestinationId(Long destinationId);
}
