package org.nektototam.easyblog.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.collections4.IterableUtils;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nektototam.easyblog.IntegrationTest;
import org.nektototam.easyblog.domain.Authors;
import org.nektototam.easyblog.repository.AuthorsRepository;
import org.nektototam.easyblog.repository.search.AuthorsSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AuthorsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AuthorsResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_URL = "AAAAAAAAAA";
    private static final String UPDATED_URL = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/authors";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/authors/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AuthorsRepository authorsRepository;

    @Autowired
    private AuthorsSearchRepository authorsSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAuthorsMockMvc;

    private Authors authors;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Authors createEntity(EntityManager em) {
        Authors authors = new Authors().name(DEFAULT_NAME).email(DEFAULT_EMAIL).url(DEFAULT_URL);
        return authors;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Authors createUpdatedEntity(EntityManager em) {
        Authors authors = new Authors().name(UPDATED_NAME).email(UPDATED_EMAIL).url(UPDATED_URL);
        return authors;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        authorsSearchRepository.deleteAll();
        assertThat(authorsSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        authors = createEntity(em);
    }

    @Test
    @Transactional
    void createAuthors() throws Exception {
        int databaseSizeBeforeCreate = authorsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        // Create the Authors
        restAuthorsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(authors)))
            .andExpect(status().isCreated());

        // Validate the Authors in the database
        List<Authors> authorsList = authorsRepository.findAll();
        assertThat(authorsList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        Authors testAuthors = authorsList.get(authorsList.size() - 1);
        assertThat(testAuthors.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAuthors.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testAuthors.getUrl()).isEqualTo(DEFAULT_URL);
    }

    @Test
    @Transactional
    void createAuthorsWithExistingId() throws Exception {
        // Create the Authors with an existing ID
        authors.setId(1L);

        int databaseSizeBeforeCreate = authorsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorsSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restAuthorsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(authors)))
            .andExpect(status().isBadRequest());

        // Validate the Authors in the database
        List<Authors> authorsList = authorsRepository.findAll();
        assertThat(authorsList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = authorsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        // set the field null
        authors.setName(null);

        // Create the Authors, which fails.

        restAuthorsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(authors)))
            .andExpect(status().isBadRequest());

        List<Authors> authorsList = authorsRepository.findAll();
        assertThat(authorsList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllAuthors() throws Exception {
        // Initialize the database
        authorsRepository.saveAndFlush(authors);

        // Get all the authorsList
        restAuthorsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(authors.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)));
    }

    @Test
    @Transactional
    void getAuthors() throws Exception {
        // Initialize the database
        authorsRepository.saveAndFlush(authors);

        // Get the authors
        restAuthorsMockMvc
            .perform(get(ENTITY_API_URL_ID, authors.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(authors.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.url").value(DEFAULT_URL));
    }

    @Test
    @Transactional
    void getNonExistingAuthors() throws Exception {
        // Get the authors
        restAuthorsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAuthors() throws Exception {
        // Initialize the database
        authorsRepository.saveAndFlush(authors);

        int databaseSizeBeforeUpdate = authorsRepository.findAll().size();
        authorsSearchRepository.save(authors);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorsSearchRepository.findAll());

        // Update the authors
        Authors updatedAuthors = authorsRepository.findById(authors.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAuthors are not directly saved in db
        em.detach(updatedAuthors);
        updatedAuthors.name(UPDATED_NAME).email(UPDATED_EMAIL).url(UPDATED_URL);

        restAuthorsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedAuthors.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedAuthors))
            )
            .andExpect(status().isOk());

        // Validate the Authors in the database
        List<Authors> authorsList = authorsRepository.findAll();
        assertThat(authorsList).hasSize(databaseSizeBeforeUpdate);
        Authors testAuthors = authorsList.get(authorsList.size() - 1);
        assertThat(testAuthors.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAuthors.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testAuthors.getUrl()).isEqualTo(UPDATED_URL);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Authors> authorsSearchList = IterableUtils.toList(authorsSearchRepository.findAll());
                Authors testAuthorsSearch = authorsSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testAuthorsSearch.getName()).isEqualTo(UPDATED_NAME);
                assertThat(testAuthorsSearch.getEmail()).isEqualTo(UPDATED_EMAIL);
                assertThat(testAuthorsSearch.getUrl()).isEqualTo(UPDATED_URL);
            });
    }

    @Test
    @Transactional
    void putNonExistingAuthors() throws Exception {
        int databaseSizeBeforeUpdate = authorsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        authors.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuthorsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, authors.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(authors))
            )
            .andExpect(status().isBadRequest());

        // Validate the Authors in the database
        List<Authors> authorsList = authorsRepository.findAll();
        assertThat(authorsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchAuthors() throws Exception {
        int databaseSizeBeforeUpdate = authorsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        authors.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(authors))
            )
            .andExpect(status().isBadRequest());

        // Validate the Authors in the database
        List<Authors> authorsList = authorsRepository.findAll();
        assertThat(authorsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAuthors() throws Exception {
        int databaseSizeBeforeUpdate = authorsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        authors.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorsMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(authors)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Authors in the database
        List<Authors> authorsList = authorsRepository.findAll();
        assertThat(authorsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateAuthorsWithPatch() throws Exception {
        // Initialize the database
        authorsRepository.saveAndFlush(authors);

        int databaseSizeBeforeUpdate = authorsRepository.findAll().size();

        // Update the authors using partial update
        Authors partialUpdatedAuthors = new Authors();
        partialUpdatedAuthors.setId(authors.getId());

        restAuthorsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAuthors.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAuthors))
            )
            .andExpect(status().isOk());

        // Validate the Authors in the database
        List<Authors> authorsList = authorsRepository.findAll();
        assertThat(authorsList).hasSize(databaseSizeBeforeUpdate);
        Authors testAuthors = authorsList.get(authorsList.size() - 1);
        assertThat(testAuthors.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAuthors.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testAuthors.getUrl()).isEqualTo(DEFAULT_URL);
    }

    @Test
    @Transactional
    void fullUpdateAuthorsWithPatch() throws Exception {
        // Initialize the database
        authorsRepository.saveAndFlush(authors);

        int databaseSizeBeforeUpdate = authorsRepository.findAll().size();

        // Update the authors using partial update
        Authors partialUpdatedAuthors = new Authors();
        partialUpdatedAuthors.setId(authors.getId());

        partialUpdatedAuthors.name(UPDATED_NAME).email(UPDATED_EMAIL).url(UPDATED_URL);

        restAuthorsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAuthors.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAuthors))
            )
            .andExpect(status().isOk());

        // Validate the Authors in the database
        List<Authors> authorsList = authorsRepository.findAll();
        assertThat(authorsList).hasSize(databaseSizeBeforeUpdate);
        Authors testAuthors = authorsList.get(authorsList.size() - 1);
        assertThat(testAuthors.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAuthors.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testAuthors.getUrl()).isEqualTo(UPDATED_URL);
    }

    @Test
    @Transactional
    void patchNonExistingAuthors() throws Exception {
        int databaseSizeBeforeUpdate = authorsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        authors.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuthorsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, authors.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(authors))
            )
            .andExpect(status().isBadRequest());

        // Validate the Authors in the database
        List<Authors> authorsList = authorsRepository.findAll();
        assertThat(authorsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAuthors() throws Exception {
        int databaseSizeBeforeUpdate = authorsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        authors.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(authors))
            )
            .andExpect(status().isBadRequest());

        // Validate the Authors in the database
        List<Authors> authorsList = authorsRepository.findAll();
        assertThat(authorsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAuthors() throws Exception {
        int databaseSizeBeforeUpdate = authorsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        authors.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorsMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(authors)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Authors in the database
        List<Authors> authorsList = authorsRepository.findAll();
        assertThat(authorsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteAuthors() throws Exception {
        // Initialize the database
        authorsRepository.saveAndFlush(authors);
        authorsRepository.save(authors);
        authorsSearchRepository.save(authors);

        int databaseSizeBeforeDelete = authorsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the authors
        restAuthorsMockMvc
            .perform(delete(ENTITY_API_URL_ID, authors.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Authors> authorsList = authorsRepository.findAll();
        assertThat(authorsList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(authorsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchAuthors() throws Exception {
        // Initialize the database
        authors = authorsRepository.saveAndFlush(authors);
        authorsSearchRepository.save(authors);

        // Search the authors
        restAuthorsMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + authors.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(authors.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)));
    }
}
