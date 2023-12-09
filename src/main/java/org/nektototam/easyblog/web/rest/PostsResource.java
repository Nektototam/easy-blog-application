package org.nektototam.easyblog.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.nektototam.easyblog.domain.Posts;
import org.nektototam.easyblog.repository.PostsRepository;
import org.nektototam.easyblog.repository.search.PostsSearchRepository;
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
 * REST controller for managing {@link org.nektototam.easyblog.domain.Posts}.
 */
@RestController
@RequestMapping("/api/posts")
@Transactional
public class PostsResource {

    private final Logger log = LoggerFactory.getLogger(PostsResource.class);

    private static final String ENTITY_NAME = "posts";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PostsRepository postsRepository;

    private final PostsSearchRepository postsSearchRepository;

    public PostsResource(PostsRepository postsRepository, PostsSearchRepository postsSearchRepository) {
        this.postsRepository = postsRepository;
        this.postsSearchRepository = postsSearchRepository;
    }

    /**
     * {@code POST  /posts} : Create a new posts.
     *
     * @param posts the posts to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new posts, or with status {@code 400 (Bad Request)} if the posts has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Posts> createPosts(@RequestBody Posts posts) throws URISyntaxException {
        log.debug("REST request to save Posts : {}", posts);
        if (posts.getId() != null) {
            throw new BadRequestAlertException("A new posts cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Posts result = postsRepository.save(posts);
        postsSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/posts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /posts/:id} : Updates an existing posts.
     *
     * @param id the id of the posts to save.
     * @param posts the posts to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated posts,
     * or with status {@code 400 (Bad Request)} if the posts is not valid,
     * or with status {@code 500 (Internal Server Error)} if the posts couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Posts> updatePosts(@PathVariable(value = "id", required = false) final Long id, @RequestBody Posts posts)
        throws URISyntaxException {
        log.debug("REST request to update Posts : {}, {}", id, posts);
        if (posts.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, posts.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!postsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Posts result = postsRepository.save(posts);
        postsSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, posts.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /posts/:id} : Partial updates given fields of an existing posts, field will ignore if it is null
     *
     * @param id the id of the posts to save.
     * @param posts the posts to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated posts,
     * or with status {@code 400 (Bad Request)} if the posts is not valid,
     * or with status {@code 404 (Not Found)} if the posts is not found,
     * or with status {@code 500 (Internal Server Error)} if the posts couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Posts> partialUpdatePosts(@PathVariable(value = "id", required = false) final Long id, @RequestBody Posts posts)
        throws URISyntaxException {
        log.debug("REST request to partial update Posts partially : {}, {}", id, posts);
        if (posts.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, posts.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!postsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Posts> result = postsRepository
            .findById(posts.getId())
            .map(existingPosts -> {
                if (posts.getTitle() != null) {
                    existingPosts.setTitle(posts.getTitle());
                }
                if (posts.getSlug() != null) {
                    existingPosts.setSlug(posts.getSlug());
                }
                if (posts.getHtml() != null) {
                    existingPosts.setHtml(posts.getHtml());
                }
                if (posts.getCreatedAt() != null) {
                    existingPosts.setCreatedAt(posts.getCreatedAt());
                }
                if (posts.getUpdatedAt() != null) {
                    existingPosts.setUpdatedAt(posts.getUpdatedAt());
                }
                if (posts.getPublishedAt() != null) {
                    existingPosts.setPublishedAt(posts.getPublishedAt());
                }
                if (posts.getStatus() != null) {
                    existingPosts.setStatus(posts.getStatus());
                }
                if (posts.getVisibility() != null) {
                    existingPosts.setVisibility(posts.getVisibility());
                }

                return existingPosts;
            })
            .map(postsRepository::save)
            .map(savedPosts -> {
                postsSearchRepository.index(savedPosts);
                return savedPosts;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, posts.getId().toString())
        );
    }

    /**
     * {@code GET  /posts} : get all the posts.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of posts in body.
     */
    @GetMapping("")
    public List<Posts> getAllPosts(@RequestParam(required = false, defaultValue = "true") boolean eagerload) {
        log.debug("REST request to get all Posts");
        if (eagerload) {
            return postsRepository.findAllWithEagerRelationships();
        } else {
            return postsRepository.findAll();
        }
    }

    /**
     * {@code GET  /posts/:id} : get the "id" posts.
     *
     * @param id the id of the posts to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the posts, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Posts> getPosts(@PathVariable Long id) {
        log.debug("REST request to get Posts : {}", id);
        Optional<Posts> posts = postsRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(posts);
    }

    /**
     * {@code DELETE  /posts/:id} : delete the "id" posts.
     *
     * @param id the id of the posts to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosts(@PathVariable Long id) {
        log.debug("REST request to delete Posts : {}", id);
        postsRepository.deleteById(id);
        postsSearchRepository.deleteFromIndexById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /posts/_search?query=:query} : search for the posts corresponding
     * to the query.
     *
     * @param query the query of the posts search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<Posts> searchPosts(@RequestParam String query) {
        log.debug("REST request to search Posts for query {}", query);
        try {
            return StreamSupport.stream(postsSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
