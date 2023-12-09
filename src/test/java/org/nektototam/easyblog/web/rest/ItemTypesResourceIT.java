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
import org.nektototam.easyblog.domain.ItemTypes;
import org.nektototam.easyblog.repository.ItemTypesRepository;
import org.nektototam.easyblog.repository.search.ItemTypesSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ItemTypesResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ItemTypesResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/item-types";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/item-types/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ItemTypesRepository itemTypesRepository;

    @Autowired
    private ItemTypesSearchRepository itemTypesSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restItemTypesMockMvc;

    private ItemTypes itemTypes;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ItemTypes createEntity(EntityManager em) {
        ItemTypes itemTypes = new ItemTypes().name(DEFAULT_NAME);
        return itemTypes;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ItemTypes createUpdatedEntity(EntityManager em) {
        ItemTypes itemTypes = new ItemTypes().name(UPDATED_NAME);
        return itemTypes;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        itemTypesSearchRepository.deleteAll();
        assertThat(itemTypesSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        itemTypes = createEntity(em);
    }

    @Test
    @Transactional
    void createItemTypes() throws Exception {
        int databaseSizeBeforeCreate = itemTypesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        // Create the ItemTypes
        restItemTypesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(itemTypes)))
            .andExpect(status().isCreated());

        // Validate the ItemTypes in the database
        List<ItemTypes> itemTypesList = itemTypesRepository.findAll();
        assertThat(itemTypesList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        ItemTypes testItemTypes = itemTypesList.get(itemTypesList.size() - 1);
        assertThat(testItemTypes.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    void createItemTypesWithExistingId() throws Exception {
        // Create the ItemTypes with an existing ID
        itemTypes.setId(1L);

        int databaseSizeBeforeCreate = itemTypesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restItemTypesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(itemTypes)))
            .andExpect(status().isBadRequest());

        // Validate the ItemTypes in the database
        List<ItemTypes> itemTypesList = itemTypesRepository.findAll();
        assertThat(itemTypesList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = itemTypesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        // set the field null
        itemTypes.setName(null);

        // Create the ItemTypes, which fails.

        restItemTypesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(itemTypes)))
            .andExpect(status().isBadRequest());

        List<ItemTypes> itemTypesList = itemTypesRepository.findAll();
        assertThat(itemTypesList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllItemTypes() throws Exception {
        // Initialize the database
        itemTypesRepository.saveAndFlush(itemTypes);

        // Get all the itemTypesList
        restItemTypesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(itemTypes.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    void getItemTypes() throws Exception {
        // Initialize the database
        itemTypesRepository.saveAndFlush(itemTypes);

        // Get the itemTypes
        restItemTypesMockMvc
            .perform(get(ENTITY_API_URL_ID, itemTypes.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(itemTypes.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void getNonExistingItemTypes() throws Exception {
        // Get the itemTypes
        restItemTypesMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingItemTypes() throws Exception {
        // Initialize the database
        itemTypesRepository.saveAndFlush(itemTypes);

        int databaseSizeBeforeUpdate = itemTypesRepository.findAll().size();
        itemTypesSearchRepository.save(itemTypes);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());

        // Update the itemTypes
        ItemTypes updatedItemTypes = itemTypesRepository.findById(itemTypes.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedItemTypes are not directly saved in db
        em.detach(updatedItemTypes);
        updatedItemTypes.name(UPDATED_NAME);

        restItemTypesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedItemTypes.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedItemTypes))
            )
            .andExpect(status().isOk());

        // Validate the ItemTypes in the database
        List<ItemTypes> itemTypesList = itemTypesRepository.findAll();
        assertThat(itemTypesList).hasSize(databaseSizeBeforeUpdate);
        ItemTypes testItemTypes = itemTypesList.get(itemTypesList.size() - 1);
        assertThat(testItemTypes.getName()).isEqualTo(UPDATED_NAME);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<ItemTypes> itemTypesSearchList = IterableUtils.toList(itemTypesSearchRepository.findAll());
                ItemTypes testItemTypesSearch = itemTypesSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testItemTypesSearch.getName()).isEqualTo(UPDATED_NAME);
            });
    }

    @Test
    @Transactional
    void putNonExistingItemTypes() throws Exception {
        int databaseSizeBeforeUpdate = itemTypesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        itemTypes.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restItemTypesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, itemTypes.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(itemTypes))
            )
            .andExpect(status().isBadRequest());

        // Validate the ItemTypes in the database
        List<ItemTypes> itemTypesList = itemTypesRepository.findAll();
        assertThat(itemTypesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchItemTypes() throws Exception {
        int databaseSizeBeforeUpdate = itemTypesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        itemTypes.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restItemTypesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(itemTypes))
            )
            .andExpect(status().isBadRequest());

        // Validate the ItemTypes in the database
        List<ItemTypes> itemTypesList = itemTypesRepository.findAll();
        assertThat(itemTypesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamItemTypes() throws Exception {
        int databaseSizeBeforeUpdate = itemTypesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        itemTypes.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restItemTypesMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(itemTypes)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ItemTypes in the database
        List<ItemTypes> itemTypesList = itemTypesRepository.findAll();
        assertThat(itemTypesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateItemTypesWithPatch() throws Exception {
        // Initialize the database
        itemTypesRepository.saveAndFlush(itemTypes);

        int databaseSizeBeforeUpdate = itemTypesRepository.findAll().size();

        // Update the itemTypes using partial update
        ItemTypes partialUpdatedItemTypes = new ItemTypes();
        partialUpdatedItemTypes.setId(itemTypes.getId());

        partialUpdatedItemTypes.name(UPDATED_NAME);

        restItemTypesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedItemTypes.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedItemTypes))
            )
            .andExpect(status().isOk());

        // Validate the ItemTypes in the database
        List<ItemTypes> itemTypesList = itemTypesRepository.findAll();
        assertThat(itemTypesList).hasSize(databaseSizeBeforeUpdate);
        ItemTypes testItemTypes = itemTypesList.get(itemTypesList.size() - 1);
        assertThat(testItemTypes.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void fullUpdateItemTypesWithPatch() throws Exception {
        // Initialize the database
        itemTypesRepository.saveAndFlush(itemTypes);

        int databaseSizeBeforeUpdate = itemTypesRepository.findAll().size();

        // Update the itemTypes using partial update
        ItemTypes partialUpdatedItemTypes = new ItemTypes();
        partialUpdatedItemTypes.setId(itemTypes.getId());

        partialUpdatedItemTypes.name(UPDATED_NAME);

        restItemTypesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedItemTypes.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedItemTypes))
            )
            .andExpect(status().isOk());

        // Validate the ItemTypes in the database
        List<ItemTypes> itemTypesList = itemTypesRepository.findAll();
        assertThat(itemTypesList).hasSize(databaseSizeBeforeUpdate);
        ItemTypes testItemTypes = itemTypesList.get(itemTypesList.size() - 1);
        assertThat(testItemTypes.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void patchNonExistingItemTypes() throws Exception {
        int databaseSizeBeforeUpdate = itemTypesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        itemTypes.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restItemTypesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, itemTypes.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(itemTypes))
            )
            .andExpect(status().isBadRequest());

        // Validate the ItemTypes in the database
        List<ItemTypes> itemTypesList = itemTypesRepository.findAll();
        assertThat(itemTypesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchItemTypes() throws Exception {
        int databaseSizeBeforeUpdate = itemTypesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        itemTypes.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restItemTypesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(itemTypes))
            )
            .andExpect(status().isBadRequest());

        // Validate the ItemTypes in the database
        List<ItemTypes> itemTypesList = itemTypesRepository.findAll();
        assertThat(itemTypesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamItemTypes() throws Exception {
        int databaseSizeBeforeUpdate = itemTypesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        itemTypes.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restItemTypesMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(itemTypes))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ItemTypes in the database
        List<ItemTypes> itemTypesList = itemTypesRepository.findAll();
        assertThat(itemTypesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteItemTypes() throws Exception {
        // Initialize the database
        itemTypesRepository.saveAndFlush(itemTypes);
        itemTypesRepository.save(itemTypes);
        itemTypesSearchRepository.save(itemTypes);

        int databaseSizeBeforeDelete = itemTypesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the itemTypes
        restItemTypesMockMvc
            .perform(delete(ENTITY_API_URL_ID, itemTypes.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ItemTypes> itemTypesList = itemTypesRepository.findAll();
        assertThat(itemTypesList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(itemTypesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchItemTypes() throws Exception {
        // Initialize the database
        itemTypes = itemTypesRepository.saveAndFlush(itemTypes);
        itemTypesSearchRepository.save(itemTypes);

        // Search the itemTypes
        restItemTypesMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + itemTypes.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(itemTypes.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }
}
