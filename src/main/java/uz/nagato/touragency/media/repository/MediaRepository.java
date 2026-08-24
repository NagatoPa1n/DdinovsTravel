package uz.nagato.touragency.media.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.nagato.touragency.media.entity.Media;
import uz.nagato.touragency.media.entity.OwnerType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media, Long>, JpaSpecificationExecutor<Media> {

    Optional<Media> findByFileName(String fileName);

    List<Media> findAllByOwnerTypeAndOwnerIdOrderBySortOrderAscIdAsc(OwnerType ownerType, Long ownerId);

    List<Media> findAllByOwnerTypeAndOwnerIdInOrderBySortOrderAscIdAsc(OwnerType ownerType,
                                                                      Collection<Long> ownerIds);

    Page<Media> findAllByOwnerType(OwnerType ownerType, Pageable pageable);
}
