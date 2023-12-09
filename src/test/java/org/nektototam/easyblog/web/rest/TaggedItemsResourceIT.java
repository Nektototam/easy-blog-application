package org.nektototam.easyblog.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.collections4.IterableUtils;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nektototam.easyblog.IntegrationTest;
import org.nektototam.easyblog.domain.TaggedItems;
import org.nektototam.easyblog.repository.TaggedItemsRepository;
import org.nektototam.easyblog.repository.search.TaggedItemsSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TaggedItemsResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class TaggedItemsResourceIT {

    private static final String DEFAULT_ITEM_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_ITEM_TYPE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/tagged-items";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/tagged-items/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private TaggedItemsRepository taggedItemsRepository;

    @Mock
    private TaggedItemsRepository taggedItemsRepositoryMock;

    @Autowired
    private TaggedItemsSearchRepository taggedItemsSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTaggedItemsMockMvc;

    private TaggedItems taggedItems;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TaggedItems createEntity(EntityManager em) {
        TaggedItems taggedItems = new TaggedItems().itemType(DEFAULT_ITEM_TYPE);
        return taggedItems;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TaggedItems createUpdatedEntity(EntityManager em) {
        TaggedItems taggedItems = new TaggedItems().itemType(UPDATED_ITEM_TYPE);
        return taggedItems;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        taggedItemsSearchRepository.deleteAll();
        assertThat(taggedItemsSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        taggedItems = createEntity(em);
    }

    @Test
    @Transactional
    void createTaggedItems() throws Exception {
        int databaseSizeBeforeCreate = taggedItemsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        // Create the TaggedItems
        restTaggedItemsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(taggedItems)))
            .andExpect(status().isCreated());

        // Validate the TaggedItems in the database
        List<TaggedItems> taggedItemsList = taggedItemsRepository.findAll();
        assertThat(taggedItemsList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        TaggedItems testTaggedItems = taggedItemsList.get(taggedItemsList.size() - 1);
        assertThat(testTaggedItems.getItemType()).isEqualTo(DEFAULT_ITEM_TYPE);
    }

    @Test
    @Transactional
    void createTaggedItemsWithExistingId() throws Exception {
        // Create the TaggedItems with an existing ID
        taggedItems.setId(1L);

        int databaseSizeBeforeCreate = taggedItemsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restTaggedItemsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(taggedItems)))
            .andExpect(status().isBadRequest());

        // Validate the TaggedItems in the database
        List<TaggedItems> taggedItemsList = taggedItemsRepository.findAll();
        assertThat(taggedItemsList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkItemTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = taggedItemsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        // set the field null
        taggedItems.setItemType(null);

        // Create the TaggedItems, which fails.

        restTaggedItemsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(taggedItems)))
            .andExpect(status().isBadRequest());

        List<TaggedItems> taggedItemsList = taggedItemsRepository.findAll();
        assertThat(taggedItemsList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllTaggedItems() throws Exception {
        // Initialize the database
        taggedItemsRepository.saveAndFlush(taggedItems);

        // Get all the taggedItemsList
        restTaggedItemsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(taggedItems.getId().intValue())))
            .andExpect(jsonPath("$.[*].itemType").value(hasItem(DEFAULT_ITEM_TYPE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTaggedItemsWithEagerRelationshipsIsEnabled() throws Exception {
        when(taggedItemsRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTaggedItemsMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(taggedItemsRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTaggedItemsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(taggedItemsRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTaggedItemsMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(taggedItemsRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getTaggedItems() throws Exception {
        // Initialize the database
        taggedItemsRepository.saveAndFlush(taggedItems);

        // Get the taggedItems
        restTaggedItemsMockMvc
            .perform(get(ENTITY_API_URL_ID, taggedItems.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(taggedItems.getId().intValue()))
            .andExpect(jsonPath("$.itemType").value(DEFAULT_ITEM_TYPE));
    }

    @Test
    @Transactional
    void getNonExistingTaggedItems() throws Exception {
        // Get the taggedItems
        restTaggedItemsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTaggedItems() throws Exception {
        // Initialize the database
        taggedItemsRepository.saveAndFlush(taggedItems);

        int databaseSizeBeforeUpdate = taggedItemsRepository.findAll().size();
        taggedItemsSearchRepository.save(taggedItems);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());

        // Update the taggedItems
        TaggedItems updatedTaggedItems = taggedItemsRepository.findById(taggedItems.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTaggedItems are not directly saved in db
        em.detach(updatedTaggedItems);
        updatedTaggedItems.itemType(UPDATED_ITEM_TYPE);

        restTaggedItemsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedTaggedItems.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedTaggedItems))
            )
            .andExpect(status().isOk());

        // Validate the TaggedItems in the database
        List<TaggedItems> taggedItemsList = taggedItemsRepository.findAll();
        assertThat(taggedItemsList).hasSize(databaseSizeBeforeUpdate);
        TaggedItems testTaggedItems = taggedItemsList.get(taggedItemsList.size() - 1);
        assertThat(testTaggedItems.getItemType()).isEqualTo(UPDATED_ITEM_TYPE);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<TaggedItems> taggedItemsSearchList = IterableUtils.toList(taggedItemsSearchRepository.findAll());
                TaggedItems testTaggedItemsSearch = taggedItemsSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testTaggedItemsSearch.getItemType()).isEqualTo(UPDATED_ITEM_TYPE);
            });
    }

    @Test
    @Transactional
    void putNonExistingTaggedItems() throws Exception {
        int databaseSizeBeforeUpdate = taggedItemsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        taggedItems.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTaggedItemsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, taggedItems.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(taggedItems))
            )
            .andExpect(status().isBadRequest());

        // Validate the TaggedItems in the database
        List<TaggedItems> taggedItemsList = taggedItemsRepository.findAll();
        assertThat(taggedItemsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchTaggedItems() throws Exception {
        int databaseSizeBeforeUpdate = taggedItemsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        taggedItems.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaggedItemsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(taggedItems))
            )
            .andExpect(status().isBadRequest());

        // Validate the TaggedItems in the database
        List<TaggedItems> taggedItemsList = taggedItemsRepository.findAll();
        assertThat(taggedItemsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTaggedItems() throws Exception {
        int databaseSizeBeforeUpdate = taggedItemsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        taggedItems.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaggedItemsMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(taggedItems)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TaggedItems in the database
        List<TaggedItems> taggedItemsList = taggedItemsRepository.findAll();
        assertThat(taggedItemsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateTaggedItemsWithPatch() throws Exception {
        // Initialize the database
        taggedItemsRepository.saveAndFlush(taggedItems);

        int databaseSizeBeforeUpdate = taggedItemsRepository.findAll().size();

        // Update the taggedItems using partial update
        TaggedItems partialUpdatedTaggedItems = new TaggedItems();
        partialUpdatedTaggedItems.setId(taggedItems.getId());

        partialUpdatedTaggedItems.itemType(UPDATED_ITEM_TYPE);

        restTaggedItemsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTaggedItems.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTaggedItems))
            )
            .andExpect(status().isOk());

        // Validate the TaggedItems in the database
        List<TaggedItems> taggedItemsList = taggedItemsRepository.findAll();
        assertThat(taggedItemsList).hasSize(databaseSizeBeforeUpdate);
        TaggedItems testTaggedItems = taggedItemsList.get(taggedItemsList.size() - 1);
        assertThat(testTaggedItems.getItemType()).isEqualTo(UPDATED_ITEM_TYPE);
    }

    @Test
    @Transactional
    void fullUpdateTaggedItemsWithPatch() throws Exception {
        // Initialize the database
        taggedItemsRepository.saveAndFlush(taggedItems);

        int databaseSizeBeforeUpdate = taggedItemsRepository.findAll().size();

        // Update the taggedItems using partial update
        TaggedItems partialUpdatedTaggedItems = new TaggedItems();
        partialUpdatedTaggedItems.setId(taggedItems.getId());

        partialUpdatedTaggedItems.itemType(UPDATED_ITEM_TYPE);

        restTaggedItemsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTaggedItems.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTaggedItems))
            )
            .andExpect(status().isOk());

        // Validate the TaggedItems in the database
        List<TaggedItems> taggedItemsList = taggedItemsRepository.findAll();
        assertThat(taggedItemsList).hasSize(databaseSizeBeforeUpdate);
        TaggedItems testTaggedItems = taggedItemsList.get(taggedItemsList.size() - 1);
        assertThat(testTaggedItems.getItemType()).isEqualTo(UPDATED_ITEM_TYPE);
    }

    @Test
    @Transactional
    void patchNonExistingTaggedItems() throws Exception {
        int databaseSizeBeforeUpdate = taggedItemsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        taggedItems.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTaggedItemsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, taggedItems.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(taggedItems))
            )
            .andExpect(status().isBadRequest());

        // Validate the TaggedItems in the database
        List<TaggedItems> taggedItemsList = taggedItemsRepository.findAll();
        assertThat(taggedItemsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTaggedItems() throws Exception {
        int databaseSizeBeforeUpdate = taggedItemsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        taggedItems.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaggedItemsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(taggedItems))
            )
            .andExpect(status().isBadRequest());

        // Validate the TaggedItems in the database
        List<TaggedItems> taggedItemsList = taggedItemsRepository.findAll();
        assertThat(taggedItemsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTaggedItems() throws Exception {
        int databaseSizeBeforeUpdate = taggedItemsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        taggedItems.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaggedItemsMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(taggedItems))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the TaggedItems in the database
        List<TaggedItems> taggedItemsList = taggedItemsRepository.findAll();
        assertThat(taggedItemsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteTaggedItems() throws Exception {
        // Initialize the database
        taggedItemsRepository.saveAndFlush(taggedItems);
        taggedItemsRepository.save(taggedItems);
        taggedItemsSearchRepository.save(taggedItems);

        int databaseSizeBeforeDelete = taggedItemsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the taggedItems
        restTaggedItemsMockMvc
            .perform(delete(ENTITY_API_URL_ID, taggedItems.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<TaggedItems> taggedItemsList = taggedItemsRepository.findAll();
        assertThat(taggedItemsList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(taggedItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchTaggedItems() throws Exception {
        // Initialize the database
        taggedItems = taggedItemsRepository.saveAndFlush(taggedItems);
        taggedItemsSearchRepository.save(taggedItems);

        // Search the taggedItems
        restTaggedItemsMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + taggedItems.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(taggedItems.getId().intValue())))
            .andExpect(jsonPath("$.[*].itemType").value(hasItem(DEFAULT_ITEM_TYPE)));
    }
}
