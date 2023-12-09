package org.nektototam.easyblog.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.nektototam.easyblog.domain.Comments;
import org.nektototam.easyblog.repository.CommentsRepository;
import org.nektototam.easyblog.repository.search.CommentsSearchRepository;
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
 * REST controller for managing {@link org.nektototam.easyblog.domain.Comments}.
 */
@RestController
@RequestMapping("/api/comments")
@Transactional
public class CommentsResource {

    private final Logger log = LoggerFactory.getLogger(CommentsResource.class);

    private static final String ENTITY_NAME = "comments";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CommentsRepository commentsRepository;

    private final CommentsSearchRepository commentsSearchRepository;

    public CommentsResource(CommentsRepository commentsRepository, CommentsSearchRepository commentsSearchRepository) {
        this.commentsRepository = commentsRepository;
        this.commentsSearchRepository = commentsSearchRepository;
    }

    /**
     * {@code POST  /comments} : Create a new comments.
     *
     * @param comments the comments to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new comments, or with status {@code 400 (Bad Request)} if the comments has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Comments> createComments(@Valid @RequestBody Comments comments) throws URISyntaxException {
        log.debug("REST request to save Comments : {}", comments);
        if (comments.getId() != null) {
            throw new BadRequestAlertException("A new comments cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Comments result = commentsRepository.save(comments);
        commentsSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/comments/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /comments/:id} : Updates an existing comments.
     *
     * @param id the id of the comments to save.
     * @param comments the comments to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated comments,
     * or with status {@code 400 (Bad Request)} if the comments is not valid,
     * or with status {@code 500 (Internal Server Error)} if the comments couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Comments> updateComments(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Comments comments
    ) throws URISyntaxException {
        log.debug("REST request to update Comments : {}, {}", id, comments);
        if (comments.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, comments.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!commentsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Comments result = commentsRepository.save(comments);
        commentsSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, comments.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /comments/:id} : Partial updates given fields of an existing comments, field will ignore if it is null
     *
     * @param id the id of the comments to save.
     * @param comments the comments to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated comments,
     * or with status {@code 400 (Bad Request)} if the comments is not valid,
     * or with status {@code 404 (Not Found)} if the comments is not found,
     * or with status {@code 500 (Internal Server Error)} if the comments couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Comments> partialUpdateComments(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Comments comments
    ) throws URISyntaxException {
        log.debug("REST request to partial update Comments partially : {}, {}", id, comments);
        if (comments.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, comments.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!commentsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Comments> result = commentsRepository
            .findById(comments.getId())
            .map(existingComments -> {
                if (comments.getContent() != null) {
                    existingComments.setContent(comments.getContent());
                }
                if (comments.getCreatedAt() != null) {
                    existingComments.setCreatedAt(comments.getCreatedAt());
                }
                if (comments.getUpdatedAt() != null) {
                    existingComments.setUpdatedAt(comments.getUpdatedAt());
                }
                if (comments.getStatus() != null) {
                    existingComments.setStatus(comments.getStatus());
                }

                return existingComments;
            })
            .map(commentsRepository::save)
            .map(savedComments -> {
                commentsSearchRepository.index(savedComments);
                return savedComments;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, comments.getId().toString())
        );
    }

    /**
     * {@code GET  /comments} : get all the comments.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of comments in body.
     */
    @GetMapping("")
    public List<Comments> getAllComments(@RequestParam(required = false, defaultValue = "true") boolean eagerload) {
        log.debug("REST request to get all Comments");
        if (eagerload) {
            return commentsRepository.findAllWithEagerRelationships();
        } else {
            return commentsRepository.findAll();
        }
    }

    /**
     * {@code GET  /comments/:id} : get the "id" comments.
     *
     * @param id the id of the comments to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the comments, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Comments> getComments(@PathVariable Long id) {
        log.debug("REST request to get Comments : {}", id);
        Optional<Comments> comments = commentsRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(comments);
    }

    /**
     * {@code DELETE  /comments/:id} : delete the "id" comments.
     *
     * @param id the id of the comments to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComments(@PathVariable Long id) {
        log.debug("REST request to delete Comments : {}", id);
        commentsRepository.deleteById(id);
        commentsSearchRepository.deleteFromIndexById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /comments/_search?query=:query} : search for the comments corresponding
     * to the query.
     *
     * @param query the query of the comments search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<Comments> searchComments(@RequestParam String query) {
        log.debug("REST request to search Comments for query {}", query);
        try {
            return StreamSupport.stream(commentsSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
