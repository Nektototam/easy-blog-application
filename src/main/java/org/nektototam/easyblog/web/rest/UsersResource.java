package org.nektototam.easyblog.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.nektototam.easyblog.domain.Users;
import org.nektototam.easyblog.repository.UsersRepository;
import org.nektototam.easyblog.repository.search.UsersSearchRepository;
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
 * REST controller for managing {@link org.nektototam.easyblog.domain.Users}.
 */
@RestController
@RequestMapping("/api/users")
@Transactional
public class UsersResource {

    private final Logger log = LoggerFactory.getLogger(UsersResource.class);

    private static final String ENTITY_NAME = "users";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UsersRepository usersRepository;

    private final UsersSearchRepository usersSearchRepository;

    public UsersResource(UsersRepository usersRepository, UsersSearchRepository usersSearchRepository) {
        this.usersRepository = usersRepository;
        this.usersSearchRepository = usersSearchRepository;
    }

    /**
     * {@code POST  /users} : Create a new users.
     *
     * @param users the users to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new users, or with status {@code 400 (Bad Request)} if the users has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Users> createUsers(@RequestBody Users users) throws URISyntaxException {
        log.debug("REST request to save Users : {}", users);
        if (users.getId() != null) {
            throw new BadRequestAlertException("A new users cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Users result = usersRepository.save(users);
        usersSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/users/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /users/:id} : Updates an existing users.
     *
     * @param id the id of the users to save.
     * @param users the users to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated users,
     * or with status {@code 400 (Bad Request)} if the users is not valid,
     * or with status {@code 500 (Internal Server Error)} if the users couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Users> updateUsers(@PathVariable(value = "id", required = false) final Long id, @RequestBody Users users)
        throws URISyntaxException {
        log.debug("REST request to update Users : {}, {}", id, users);
        if (users.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, users.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!usersRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Users result = usersRepository.save(users);
        usersSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, users.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /users/:id} : Partial updates given fields of an existing users, field will ignore if it is null
     *
     * @param id the id of the users to save.
     * @param users the users to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated users,
     * or with status {@code 400 (Bad Request)} if the users is not valid,
     * or with status {@code 404 (Not Found)} if the users is not found,
     * or with status {@code 500 (Internal Server Error)} if the users couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Users> partialUpdateUsers(@PathVariable(value = "id", required = false) final Long id, @RequestBody Users users)
        throws URISyntaxException {
        log.debug("REST request to partial update Users partially : {}, {}", id, users);
        if (users.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, users.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!usersRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Users> result = usersRepository
            .findById(users.getId())
            .map(existingUsers -> {
                if (users.getName() != null) {
                    existingUsers.setName(users.getName());
                }
                if (users.getEmail() != null) {
                    existingUsers.setEmail(users.getEmail());
                }

                return existingUsers;
            })
            .map(usersRepository::save)
            .map(savedUsers -> {
                usersSearchRepository.index(savedUsers);
                return savedUsers;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, users.getId().toString())
        );
    }

    /**
     * {@code GET  /users} : get all the users.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of users in body.
     */
    @GetMapping("")
    public List<Users> getAllUsers() {
        log.debug("REST request to get all Users");
        return usersRepository.findAll();
    }

    /**
     * {@code GET  /users/:id} : get the "id" users.
     *
     * @param id the id of the users to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the users, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Users> getUsers(@PathVariable Long id) {
        log.debug("REST request to get Users : {}", id);
        Optional<Users> users = usersRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(users);
    }

    /**
     * {@code DELETE  /users/:id} : delete the "id" users.
     *
     * @param id the id of the users to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsers(@PathVariable Long id) {
        log.debug("REST request to delete Users : {}", id);
        usersRepository.deleteById(id);
        usersSearchRepository.deleteFromIndexById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /users/_search?query=:query} : search for the users corresponding
     * to the query.
     *
     * @param query the query of the users search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<Users> searchUsers(@RequestParam String query) {
        log.debug("REST request to search Users for query {}", query);
        try {
            return StreamSupport.stream(usersSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
