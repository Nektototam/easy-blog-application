package org.nektototam.easyblog.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.nektototam.easyblog.domain.ItemTypes;
import org.nektototam.easyblog.repository.ItemTypesRepository;
import org.nektototam.easyblog.repository.search.ItemTypesSearchRepository;
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
 * REST controller for managing {@link org.nektototam.easyblog.domain.ItemTypes}.
 */
@RestController
@RequestMapping("/api/item-types")
@Transactional
public class ItemTypesResource {

    private final Logger log = LoggerFactory.getLogger(ItemTypesResource.class);

    private static final String ENTITY_NAME = "itemTypes";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ItemTypesRepository itemTypesRepository;

    private final ItemTypesSearchRepository itemTypesSearchRepository;

    public ItemTypesResource(ItemTypesRepository itemTypesRepository, ItemTypesSearchRepository itemTypesSearchRepository) {
        this.itemTypesRepository = itemTypesRepository;
        this.itemTypesSearchRepository = itemTypesSearchRepository;
    }

    /**
     * {@code POST  /item-types} : Create a new itemTypes.
     *
     * @param itemTypes the itemTypes to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new itemTypes, or with status {@code 400 (Bad Request)} if the itemTypes has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ItemTypes> createItemTypes(@Valid @RequestBody ItemTypes itemTypes) throws URISyntaxException {
        log.debug("REST request to save ItemTypes : {}", itemTypes);
        if (itemTypes.getId() != null) {
            throw new BadRequestAlertException("A new itemTypes cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ItemTypes result = itemTypesRepository.save(itemTypes);
        itemTypesSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/item-types/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /item-types/:id} : Updates an existing itemTypes.
     *
     * @param id the id of the itemTypes to save.
     * @param itemTypes the itemTypes to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated itemTypes,
     * or with status {@code 400 (Bad Request)} if the itemTypes is not valid,
     * or with status {@code 500 (Internal Server Error)} if the itemTypes couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ItemTypes> updateItemTypes(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ItemTypes itemTypes
    ) throws URISyntaxException {
        log.debug("REST request to update ItemTypes : {}, {}", id, itemTypes);
        if (itemTypes.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, itemTypes.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!itemTypesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        ItemTypes result = itemTypesRepository.save(itemTypes);
        itemTypesSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, itemTypes.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /item-types/:id} : Partial updates given fields of an existing itemTypes, field will ignore if it is null
     *
     * @param id the id of the itemTypes to save.
     * @param itemTypes the itemTypes to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated itemTypes,
     * or with status {@code 400 (Bad Request)} if the itemTypes is not valid,
     * or with status {@code 404 (Not Found)} if the itemTypes is not found,
     * or with status {@code 500 (Internal Server Error)} if the itemTypes couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ItemTypes> partialUpdateItemTypes(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ItemTypes itemTypes
    ) throws URISyntaxException {
        log.debug("REST request to partial update ItemTypes partially : {}, {}", id, itemTypes);
        if (itemTypes.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, itemTypes.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!itemTypesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ItemTypes> result = itemTypesRepository
            .findById(itemTypes.getId())
            .map(existingItemTypes -> {
                if (itemTypes.getName() != null) {
                    existingItemTypes.setName(itemTypes.getName());
                }

                return existingItemTypes;
            })
            .map(itemTypesRepository::save)
            .map(savedItemTypes -> {
                itemTypesSearchRepository.index(savedItemTypes);
                return savedItemTypes;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, itemTypes.getId().toString())
        );
    }

    /**
     * {@code GET  /item-types} : get all the itemTypes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of itemTypes in body.
     */
    @GetMapping("")
    public List<ItemTypes> getAllItemTypes() {
        log.debug("REST request to get all ItemTypes");
        return itemTypesRepository.findAll();
    }

    /**
     * {@code GET  /item-types/:id} : get the "id" itemTypes.
     *
     * @param id the id of the itemTypes to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the itemTypes, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemTypes> getItemTypes(@PathVariable Long id) {
        log.debug("REST request to get ItemTypes : {}", id);
        Optional<ItemTypes> itemTypes = itemTypesRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(itemTypes);
    }

    /**
     * {@code DELETE  /item-types/:id} : delete the "id" itemTypes.
     *
     * @param id the id of the itemTypes to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemTypes(@PathVariable Long id) {
        log.debug("REST request to delete ItemTypes : {}", id);
        itemTypesRepository.deleteById(id);
        itemTypesSearchRepository.deleteFromIndexById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /item-types/_search?query=:query} : search for the itemTypes corresponding
     * to the query.
     *
     * @param query the query of the itemTypes search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<ItemTypes> searchItemTypes(@RequestParam String query) {
        log.debug("REST request to search ItemTypes for query {}", query);
        try {
            return StreamSupport.stream(itemTypesSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
