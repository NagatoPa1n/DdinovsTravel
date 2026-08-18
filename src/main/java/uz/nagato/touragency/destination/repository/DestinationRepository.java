package uz.nagato.touragency.destination.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.nagato.touragency.destination.entity.Destination;

import java.util.Optional;

public interface DestinationRepository extends JpaRepository<Destination, Long> {

    Optional<Destination> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    /**
     * Free-text search over name, country and city. An empty {@code search} matches everything,
     * which keeps the query free of null-parameter comparisons.
     */
    @Query("""
            select d from Destination d
            where (d.active = true or :onlyActive = false)
              and (lower(d.name) like lower(concat('%', :search, '%'))
                   or lower(d.country) like lower(concat('%', :search, '%'))
                   or lower(coalesce(d.city, '')) like lower(concat('%', :search, '%')))
            """)
    Page<Destination> search(@Param("search") String search,
                             @Param("onlyActive") boolean onlyActive,
                             Pageable pageable);
}
