package org.nektototam.easyblog.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.nektototam.easyblog.domain.Authors;
import org.nektototam.easyblog.repository.AuthorsRepository;
import org.nektototam.easyblog.repository.search.AuthorsSearchRepository;
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
 * REST controller for managing {@link org.nektototam.easyblog.domain.Authors}.
 */
@RestController
@RequestMapping("/api/authors")
@Transactional
public class AuthorsResource {

    private final Logger log = LoggerFactory.getLogger(AuthorsResource.class);

    private static final String ENTITY_NAME = "authors";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AuthorsRepository authorsRepository;

    private final AuthorsSearchRepository authorsSearchRepository;

    public AuthorsResource(AuthorsRepository authorsRepository, AuthorsSearchRepository authorsSearchRepository) {
        this.authorsRepository = authorsRepository;
        this.authorsSearchRepository = authorsSearchRepository;
    }

    /**
     * {@code POST  /authors} : Create a new authors.
     *
     * @param authors the authors to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new authors, or with status {@code 400 (Bad Request)} if the authors has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Authors> createAuthors(@Valid @RequestBody Authors authors) throws URISyntaxException {
        log.debug("REST request to save Authors : {}", authors);
        if (authors.getId() != null) {
            throw new BadRequestAlertException("A new authors cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Authors result = authorsRepository.save(authors);
        authorsSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/authors/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /authors/:id} : Updates an existing authors.
     *
     * @param id the id of the authors to save.
     * @param authors the authors to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated authors,
     * or with status {@code 400 (Bad Request)} if the authors is not valid,
     * or with status {@code 500 (Internal Server Error)} if the authors couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Authors> updateAuthors(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Authors authors
    ) throws URISyntaxException {
        log.debug("REST request to update Authors : {}, {}", id, authors);
        if (authors.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, authors.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!authorsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Authors result = authorsRepository.save(authors);
        authorsSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, authors.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /authors/:id} : Partial updates given fields of an existing authors, field will ignore if it is null
     *
     * @param id the id of the authors to save.
     * @param authors the authors to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated authors,
     * or with status {@code 400 (Bad Request)} if the authors is not valid,
     * or with status {@code 404 (Not Found)} if the authors is not found,
     * or with status {@code 500 (Internal Server Error)} if the authors couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Authors> partialUpdateAuthors(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Authors authors
    ) throws URISyntaxException {
        log.debug("REST request to partial update Authors partially : {}, {}", id, authors);
        if (authors.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, authors.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!authorsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Authors> result = authorsRepository
            .findById(authors.getId())
            .map(existingAuthors -> {
                if (authors.getName() != null) {
                    existingAuthors.setName(authors.getName());
                }
                if (authors.getEmail() != null) {
                    existingAuthors.setEmail(authors.getEmail());
                }
                if (authors.getUrl() != null) {
                    existingAuthors.setUrl(authors.getUrl());
                }

                return existingAuthors;
            })
            .map(authorsRepository::save)
            .map(savedAuthors -> {
                authorsSearchRepository.index(savedAuthors);
                return savedAuthors;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, authors.getId().toString())
        );
    }

    /**
     * {@code GET  /authors} : get all the authors.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of authors in body.
     */
    @GetMapping("")
    public List<Authors> getAllAuthors() {
        log.debug("REST request to get all Authors");
        return authorsRepository.findAll();
    }

    /**
     * {@code GET  /authors/:id} : get the "id" authors.
     *
     * @param id the id of the authors to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the authors, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Authors> getAuthors(@PathVariable Long id) {
        log.debug("REST request to get Authors : {}", id);
        Optional<Authors> authors = authorsRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(authors);
    }

    /**
     * {@code DELETE  /authors/:id} : delete the "id" authors.
     *
     * @param id the id of the authors to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthors(@PathVariable Long id) {
        log.debug("REST request to delete Authors : {}", id);
        authorsRepository.deleteById(id);
        authorsSearchRepository.deleteFromIndexById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /authors/_search?query=:query} : search for the authors corresponding
     * to the query.
     *
     * @param query the query of the authors search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<Authors> searchAuthors(@RequestParam String query) {
        log.debug("REST request to search Authors for query {}", query);
        try {
            return StreamSupport.stream(authorsSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
