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
import org.nektototam.easyblog.domain.Tags;
import org.nektototam.easyblog.repository.TagsRepository;
import org.nektototam.easyblog.repository.search.TagsSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TagsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TagsResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/tags";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/tags/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private TagsRepository tagsRepository;

    @Autowired
    private TagsSearchRepository tagsSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTagsMockMvc;

    private Tags tags;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Tags createEntity(EntityManager em) {
        Tags tags = new Tags().name(DEFAULT_NAME).description(DEFAULT_DESCRIPTION);
        return tags;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Tags createUpdatedEntity(EntityManager em) {
        Tags tags = new Tags().name(UPDATED_NAME).description(UPDATED_DESCRIPTION);
        return tags;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        tagsSearchRepository.deleteAll();
        assertThat(tagsSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        tags = createEntity(em);
    }

    @Test
    @Transactional
    void createTags() throws Exception {
        int databaseSizeBeforeCreate = tagsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        // Create the Tags
        restTagsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(tags)))
            .andExpect(status().isCreated());

        // Validate the Tags in the database
        List<Tags> tagsList = tagsRepository.findAll();
        assertThat(tagsList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(tagsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        Tags testTags = tagsList.get(tagsList.size() - 1);
        assertThat(testTags.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testTags.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    void createTagsWithExistingId() throws Exception {
        // Create the Tags with an existing ID
        tags.setId(1L);

        int databaseSizeBeforeCreate = tagsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tagsSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restTagsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(tags)))
            .andExpect(status().isBadRequest());

        // Validate the Tags in the database
        List<Tags> tagsList = tagsRepository.findAll();
        assertThat(tagsList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = tagsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        // set the field null
        tags.setName(null);

        // Create the Tags, which fails.

        restTagsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(tags)))
            .andExpect(status().isBadRequest());

        List<Tags> tagsList = tagsRepository.findAll();
        assertThat(tagsList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllTags() throws Exception {
        // Initialize the database
        tagsRepository.saveAndFlush(tags);

        // Get all the tagsList
        restTagsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tags.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @Test
    @Transactional
    void getTags() throws Exception {
        // Initialize the database
        tagsRepository.saveAndFlush(tags);

        // Get the tags
        restTagsMockMvc
            .perform(get(ENTITY_API_URL_ID, tags.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(tags.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

    @Test
    @Transactional
    void getNonExistingTags() throws Exception {
        // Get the tags
        restTagsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTags() throws Exception {
        // Initialize the database
        tagsRepository.saveAndFlush(tags);

        int databaseSizeBeforeUpdate = tagsRepository.findAll().size();
        tagsSearchRepository.save(tags);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tagsSearchRepository.findAll());

        // Update the tags
        Tags updatedTags = tagsRepository.findById(tags.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTags are not directly saved in db
        em.detach(updatedTags);
        updatedTags.name(UPDATED_NAME).description(UPDATED_DESCRIPTION);

        restTagsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedTags.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedTags))
            )
            .andExpect(status().isOk());

        // Validate the Tags in the database
        List<Tags> tagsList = tagsRepository.findAll();
        assertThat(tagsList).hasSize(databaseSizeBeforeUpdate);
        Tags testTags = tagsList.get(tagsList.size() - 1);
        assertThat(testTags.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testTags.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(tagsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Tags> tagsSearchList = IterableUtils.toList(tagsSearchRepository.findAll());
                Tags testTagsSearch = tagsSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testTagsSearch.getName()).isEqualTo(UPDATED_NAME);
                assertThat(testTagsSearch.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
            });
    }

    @Test
    @Transactional
    void putNonExistingTags() throws Exception {
        int databaseSizeBeforeUpdate = tagsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        tags.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTagsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, tags.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(tags))
            )
            .andExpect(status().isBadRequest());

        // Validate the Tags in the database
        List<Tags> tagsList = tagsRepository.findAll();
        assertThat(tagsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchTags() throws Exception {
        int databaseSizeBeforeUpdate = tagsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        tags.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTagsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(tags))
            )
            .andExpect(status().isBadRequest());

        // Validate the Tags in the database
        List<Tags> tagsList = tagsRepository.findAll();
        assertThat(tagsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTags() throws Exception {
        int databaseSizeBeforeUpdate = tagsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        tags.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTagsMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(tags)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Tags in the database
        List<Tags> tagsList = tagsRepository.findAll();
        assertThat(tagsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateTagsWithPatch() throws Exception {
        // Initialize the database
        tagsRepository.saveAndFlush(tags);

        int databaseSizeBeforeUpdate = tagsRepository.findAll().size();

        // Update the tags using partial update
        Tags partialUpdatedTags = new Tags();
        partialUpdatedTags.setId(tags.getId());

        partialUpdatedTags.name(UPDATED_NAME).description(UPDATED_DESCRIPTION);

        restTagsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTags.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTags))
            )
            .andExpect(status().isOk());

        // Validate the Tags in the database
        List<Tags> tagsList = tagsRepository.findAll();
        assertThat(tagsList).hasSize(databaseSizeBeforeUpdate);
        Tags testTags = tagsList.get(tagsList.size() - 1);
        assertThat(testTags.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testTags.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void fullUpdateTagsWithPatch() throws Exception {
        // Initialize the database
        tagsRepository.saveAndFlush(tags);

        int databaseSizeBeforeUpdate = tagsRepository.findAll().size();

        // Update the tags using partial update
        Tags partialUpdatedTags = new Tags();
        partialUpdatedTags.setId(tags.getId());

        partialUpdatedTags.name(UPDATED_NAME).description(UPDATED_DESCRIPTION);

        restTagsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTags.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTags))
            )
            .andExpect(status().isOk());

        // Validate the Tags in the database
        List<Tags> tagsList = tagsRepository.findAll();
        assertThat(tagsList).hasSize(databaseSizeBeforeUpdate);
        Tags testTags = tagsList.get(tagsList.size() - 1);
        assertThat(testTags.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testTags.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void patchNonExistingTags() throws Exception {
        int databaseSizeBeforeUpdate = tagsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        tags.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTagsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, tags.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(tags))
            )
            .andExpect(status().isBadRequest());

        // Validate the Tags in the database
        List<Tags> tagsList = tagsRepository.findAll();
        assertThat(tagsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTags() throws Exception {
        int databaseSizeBeforeUpdate = tagsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        tags.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTagsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(tags))
            )
            .andExpect(status().isBadRequest());

        // Validate the Tags in the database
        List<Tags> tagsList = tagsRepository.findAll();
        assertThat(tagsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTags() throws Exception {
        int databaseSizeBeforeUpdate = tagsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        tags.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTagsMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(tags)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Tags in the database
        List<Tags> tagsList = tagsRepository.findAll();
        assertThat(tagsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteTags() throws Exception {
        // Initialize the database
        tagsRepository.saveAndFlush(tags);
        tagsRepository.save(tags);
        tagsSearchRepository.save(tags);

        int databaseSizeBeforeDelete = tagsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the tags
        restTagsMockMvc
            .perform(delete(ENTITY_API_URL_ID, tags.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Tags> tagsList = tagsRepository.findAll();
        assertThat(tagsList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tagsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchTags() throws Exception {
        // Initialize the database
        tags = tagsRepository.saveAndFlush(tags);
        tagsSearchRepository.save(tags);

        // Search the tags
        restTagsMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + tags.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tags.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }
}
