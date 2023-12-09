package org.nektototam.easyblog.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.nektototam.easyblog.domain.TaggedItems;
import org.nektototam.easyblog.repository.TaggedItemsRepository;
import org.nektototam.easyblog.repository.search.TaggedItemsSearchRepository;
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
 * REST controller for managing {@link org.nektototam.easyblog.domain.TaggedItems}.
 */
@RestController
@RequestMapping("/api/tagged-items")
@Transactional
public class TaggedItemsResource {

    private final Logger log = LoggerFactory.getLogger(TaggedItemsResource.class);

    private static final String ENTITY_NAME = "taggedItems";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TaggedItemsRepository taggedItemsRepository;

    private final TaggedItemsSearchRepository taggedItemsSearchRepository;

    public TaggedItemsResource(TaggedItemsRepository taggedItemsRepository, TaggedItemsSearchRepository taggedItemsSearchRepository) {
        this.taggedItemsRepository = taggedItemsRepository;
        this.taggedItemsSearchRepository = taggedItemsSearchRepository;
    }

    /**
     * {@code POST  /tagged-items} : Create a new taggedItems.
     *
     * @param taggedItems the taggedItems to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new taggedItems, or with status {@code 400 (Bad Request)} if the taggedItems has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<TaggedItems> createTaggedItems(@Valid @RequestBody TaggedItems taggedItems) throws URISyntaxException {
        log.debug("REST request to save TaggedItems : {}", taggedItems);
        if (taggedItems.getId() != null) {
            throw new BadRequestAlertException("A new taggedItems cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TaggedItems result = taggedItemsRepository.save(taggedItems);
        taggedItemsSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/tagged-items/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /tagged-items/:id} : Updates an existing taggedItems.
     *
     * @param id the id of the taggedItems to save.
     * @param taggedItems the taggedItems to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated taggedItems,
     * or with status {@code 400 (Bad Request)} if the taggedItems is not valid,
     * or with status {@code 500 (Internal Server Error)} if the taggedItems couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaggedItems> updateTaggedItems(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TaggedItems taggedItems
    ) throws URISyntaxException {
        log.debug("REST request to update TaggedItems : {}, {}", id, taggedItems);
        if (taggedItems.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, taggedItems.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!taggedItemsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        TaggedItems result = taggedItemsRepository.save(taggedItems);
        taggedItemsSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, taggedItems.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /tagged-items/:id} : Partial updates given fields of an existing taggedItems, field will ignore if it is null
     *
     * @param id the id of the taggedItems to save.
     * @param taggedItems the taggedItems to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated taggedItems,
     * or with status {@code 400 (Bad Request)} if the taggedItems is not valid,
     * or with status {@code 404 (Not Found)} if the taggedItems is not found,
     * or with status {@code 500 (Internal Server Error)} if the taggedItems couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TaggedItems> partialUpdateTaggedItems(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TaggedItems taggedItems
    ) throws URISyntaxException {
        log.debug("REST request to partial update TaggedItems partially : {}, {}", id, taggedItems);
        if (taggedItems.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, taggedItems.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!taggedItemsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TaggedItems> result = taggedItemsRepository
            .findById(taggedItems.getId())
            .map(existingTaggedItems -> {
                if (taggedItems.getItemType() != null) {
                    existingTaggedItems.setItemType(taggedItems.getItemType());
                }

                return existingTaggedItems;
            })
            .map(taggedItemsRepository::save)
            .map(savedTaggedItems -> {
                taggedItemsSearchRepository.index(savedTaggedItems);
                return savedTaggedItems;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, taggedItems.getId().toString())
        );
    }

    /**
     * {@code GET  /tagged-items} : get all the taggedItems.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of taggedItems in body.
     */
    @GetMapping("")
    public List<TaggedItems> getAllTaggedItems(@RequestParam(required = false, defaultValue = "true") boolean eagerload) {
        log.debug("REST request to get all TaggedItems");
        if (eagerload) {
            return taggedItemsRepository.findAllWithEagerRelationships();
        } else {
            return taggedItemsRepository.findAll();
        }
    }

    /**
     * {@code GET  /tagged-items/:id} : get the "id" taggedItems.
     *
     * @param id the id of the taggedItems to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the taggedItems, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaggedItems> getTaggedItems(@PathVariable Long id) {
        log.debug("REST request to get TaggedItems : {}", id);
        Optional<TaggedItems> taggedItems = taggedItemsRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(taggedItems);
    }

    /**
     * {@code DELETE  /tagged-items/:id} : delete the "id" taggedItems.
     *
     * @param id the id of the taggedItems to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaggedItems(@PathVariable Long id) {
        log.debug("REST request to delete TaggedItems : {}", id);
        taggedItemsRepository.deleteById(id);
        taggedItemsSearchRepository.deleteFromIndexById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /tagged-items/_search?query=:query} : search for the taggedItems corresponding
     * to the query.
     *
     * @param query the query of the taggedItems search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<TaggedItems> searchTaggedItems(@RequestParam String query) {
        log.debug("REST request to search TaggedItems for query {}", query);
        try {
            return StreamSupport.stream(taggedItemsSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
