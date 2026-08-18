package uz.nagato.touragency.destination.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.nagato.touragency.common.exception.ConflictException;
import uz.nagato.touragency.common.exception.NotFoundException;
import uz.nagato.touragency.common.response.PageResponse;
import uz.nagato.touragency.common.util.SlugUtils;
import uz.nagato.touragency.destination.dto.DestinationRequest;
import uz.nagato.touragency.destination.dto.DestinationResponse;
import uz.nagato.touragency.destination.entity.Destination;
import uz.nagato.touragency.destination.repository.DestinationRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DestinationService {

    private final DestinationRepository destinationRepository;

    public PageResponse<DestinationResponse> search(String search, boolean onlyActive, Pageable pageable) {
        String term = search == null ? "" : search.trim();
        return PageResponse.of(
                destinationRepository.search(term, onlyActive, pageable).map(DestinationResponse::from));
    }

    public DestinationResponse findById(Long id) {
        return DestinationResponse.from(getEntity(id));
    }

    public DestinationResponse findBySlug(String slug) {
        Destination destination = destinationRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Destination not found with slug: " + slug));
        return DestinationResponse.from(destination);
    }

    /** Entity lookup for other modules (tours reference destinations). */
    public Destination getEntity(Long id) {
        return destinationRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Destination", id));
    }

    @Transactional
    public DestinationResponse create(DestinationRequest request) {
        String slug = SlugUtils.slugOrDerive(request.slug(), request.name());
        if (destinationRepository.existsBySlug(slug)) {
            throw new ConflictException("Destination slug already in use: " + slug);
        }
        Destination destination = new Destination();
        apply(destination, request, slug);
        return DestinationResponse.from(destinationRepository.save(destination));
    }

    @Transactional
    public DestinationResponse update(Long id, DestinationRequest request) {
        Destination destination = getEntity(id);
        String slug = SlugUtils.slugOrDerive(request.slug(), request.name());
        if (destinationRepository.existsBySlugAndIdNot(slug, id)) {
            throw new ConflictException("Destination slug already in use: " + slug);
        }
        apply(destination, request, slug);
        return DestinationResponse.from(destinationRepository.save(destination));
    }

    @Transactional
    public void delete(Long id) {
        destinationRepository.delete(getEntity(id));
    }

    private void apply(Destination destination, DestinationRequest request, String slug) {
        destination.setName(request.name());
        destination.setSlug(slug);
        destination.setCountry(request.country());
        destination.setCity(request.city());
        destination.setDescription(request.description());
        destination.setCoverImageUrl(request.coverImageUrl());
        destination.setLatitude(request.latitude());
        destination.setLongitude(request.longitude());
        destination.setActive(request.active() == null || request.active());
    }
}
