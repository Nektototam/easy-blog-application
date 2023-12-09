package org.nektototam.easyblog.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.nektototam.easyblog.domain.Pages;
import org.nektototam.easyblog.repository.PagesRepository;
import org.nektototam.easyblog.repository.search.PagesSearchRepository;
import org.nektototam.easyblog.web.rest.errors.BadRequestAlertException;
import org.nektototam.easyblog.web.rest.errors.ElasticsearchExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link org.nektototam.easyblog.domain.Pages}.
 */
@RestController
@RequestMapping("/api/pages")
@Transactional
public class PagesResource {

    private final Logger log = LoggerFactory.getLogger(PagesResource.class);

    private static final String ENTITY_NAME = "pages";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PagesRepository pagesRepository;

    private final PagesSearchRepository pagesSearchRepository;

    public PagesResource(PagesRepository pagesRepository, PagesSearchRepository pagesSearchRepository) {
        this.pagesRepository = pagesRepository;
        this.pagesSearchRepository = pagesSearchRepository;
    }

    /**
     * {@code POST  /pages} : Create a new pages.
     *
     * @param pages the pages to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new pages, or with status {@code 400 (Bad Request)} if the pages has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Pages> createPages(@RequestBody Pages pages) throws URISyntaxException {
        log.debug("REST request to save Pages : {}", pages);
        if (pages.getId() != null) {
            throw new BadRequestAlertException("A new pages cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Pages result = pagesRepository.save(pages);
        pagesSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/pages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /pages/:id} : Updates an existing pages.
     *
     * @param id the id of the pages to save.
     * @param pages the pages to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated pages,
     * or with status {@code 400 (Bad Request)} if the pages is not valid,
     * or with status {@code 500 (Internal Server Error)} if the pages couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Pages> updatePages(@PathVariable(value = "id", required = false) final Long id, @RequestBody Pages pages)
        throws URISyntaxException {
        log.debug("REST request to update Pages : {}, {}", id, pages);
        if (pages.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, pages.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!pagesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Pages result = pagesRepository.save(pages);
        pagesSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, pages.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /pages/:id} : Partial updates given fields of an existing pages, field will ignore if it is null
     *
     * @param id the id of the pages to save.
     * @param pages the pages to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated pages,
     * or with status {@code 400 (Bad Request)} if the pages is not valid,
     * or with status {@code 404 (Not Found)} if the pages is not found,
     * or with status {@code 500 (Internal Server Error)} if the pages couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Pages> partialUpdatePages(@PathVariable(value = "id", required = false) final Long id, @RequestBody Pages pages)
        throws URISyntaxException {
        log.debug("REST request to partial update Pages partially : {}, {}", id, pages);
        if (pages.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, pages.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!pagesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Pages> result = pagesRepository
            .findById(pages.getId())
            .map(existingPages -> {
                if (pages.getTitle() != null) {
                    existingPages.setTitle(pages.getTitle());
                }
                if (pages.getSlug() != null) {
                    existingPages.setSlug(pages.getSlug());
                }
                if (pages.getHtml() != null) {
                    existingPages.setHtml(pages.getHtml());
                }
                if (pages.getCreatedAt() != null) {
                    existingPages.setCreatedAt(pages.getCreatedAt());
                }
                if (pages.getUpdatedAt() != null) {
                    existingPages.setUpdatedAt(pages.getUpdatedAt());
                }
                if (pages.getPublishedAt() != null) {
                    existingPages.setPublishedAt(pages.getPublishedAt());
                }
                if (pages.getStatus() != null) {
                    existingPages.setStatus(pages.getStatus());
                }
                if (pages.getVisibility() != null) {
                    existingPages.setVisibility(pages.getVisibility());
                }

                return existingPages;
            })
            .map(pagesRepository::save)
            .map(savedPages -> {
                pagesSearchRepository.index(savedPages);
                return savedPages;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, pages.getId().toString())
        );
    }

    /**
     * {@code GET  /pages} : get all the pages.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of pages in body.
     */
    @GetMapping("")
    public List<Pages> getAllPages(@RequestParam(required = false, defaultValue = "true") boolean eagerload) {
        log.debug("REST request to get all Pages");
        if (eagerload) {
            return pagesRepository.findAllWithEagerRelationships();
        } else {
            return pagesRepository.findAll();
        }
    }

    /**
     * {@code GET  /pages/:id} : get the "id" pages.
     *
     * @param id the id of the pages to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the pages, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Pages> getPages(@PathVariable Long id) {
        log.debug("REST request to get Pages : {}", id);
        Optional<Pages> pages = pagesRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(pages);
    }

    /**
     * {@code DELETE  /pages/:id} : delete the "id" pages.
     *
     * @param id the id of the pages to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePages(@PathVariable Long id) {
        log.debug("REST request to delete Pages : {}", id);
        pagesRepository.deleteById(id);
        pagesSearchRepository.deleteFromIndexById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /pages/_search?query=:query} : search for the pages corresponding
     * to the query.
     *
     * @param query the query of the pages search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<Pages> searchPages(@RequestParam String query) {
        log.debug("REST request to search Pages for query {}", query);
        try {
            return StreamSupport.stream(pagesSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
