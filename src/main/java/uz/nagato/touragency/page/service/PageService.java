package uz.nagato.touragency.page.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.nagato.touragency.common.exception.ConflictException;
import uz.nagato.touragency.common.exception.NotFoundException;
import uz.nagato.touragency.common.util.SlugUtils;
import uz.nagato.touragency.page.dto.PageDto;
import uz.nagato.touragency.page.dto.PageRequest;
import uz.nagato.touragency.page.entity.Page;
import uz.nagato.touragency.page.repository.PageRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PageService {

    private final PageRepository pageRepository;

    /** Content pages are few, so the listing is unpaged on purpose. */
    public List<PageDto> findAll(boolean onlyPublished) {
        List<Page> pages = onlyPublished
                ? pageRepository.findAllByPublishedTrueOrderBySortOrderAscTitleAsc()
                : pageRepository.findAllByOrderBySortOrderAscTitleAsc();
        return pages.stream().map(PageDto::from).toList();
    }

    public PageDto findBySlug(String slug, boolean onlyPublished) {
        Page page = (onlyPublished
                ? pageRepository.findBySlugAndPublishedTrue(slug)
                : pageRepository.findBySlug(slug))
                .orElseThrow(() -> new NotFoundException("Page not found with slug: " + slug));
        return PageDto.from(page);
    }

    public PageDto findById(Long id) {
        return PageDto.from(getEntity(id));
    }

    @Transactional
    public PageDto create(PageRequest request) {
        String slug = SlugUtils.slugOrDerive(request.slug(), request.title());
        if (pageRepository.existsBySlug(slug)) {
            throw new ConflictException("Page slug already in use: " + slug);
        }
        Page page = new Page();
        apply(page, request, slug);
        return PageDto.from(pageRepository.save(page));
    }

    @Transactional
    public PageDto update(Long id, PageRequest request) {
        Page page = getEntity(id);
        String slug = SlugUtils.slugOrDerive(request.slug(), request.title());
        if (pageRepository.existsBySlugAndIdNot(slug, id)) {
            throw new ConflictException("Page slug already in use: " + slug);
        }
        apply(page, request, slug);
        return PageDto.from(pageRepository.save(page));
    }

    @Transactional
    public void delete(Long id) {
        pageRepository.delete(getEntity(id));
    }

    private Page getEntity(Long id) {
        return pageRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Page", id));
    }

    private void apply(Page page, PageRequest request, String slug) {
        page.setTitle(request.title());
        page.setSlug(slug);
        page.setContent(request.content());
        page.setExcerpt(request.excerpt());
        page.setMetaTitle(request.metaTitle());
        page.setMetaDescription(request.metaDescription());
        page.setPublished(request.published() != null && request.published());
        page.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    }
}
