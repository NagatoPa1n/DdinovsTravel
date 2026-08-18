package uz.nagato.touragency.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.nagato.touragency.category.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    List<Category> findAllByActiveTrueOrderByNameAsc();

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
