package org.nektototam.easyblog.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.nektototam.easyblog.web.rest.TestUtil.sameInstant;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
import org.nektototam.easyblog.domain.Pages;
import org.nektototam.easyblog.domain.enumeration.Status;
import org.nektototam.easyblog.domain.enumeration.Visibility;
import org.nektototam.easyblog.repository.PagesRepository;
import org.nektototam.easyblog.repository.search.PagesSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link PagesResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class PagesResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_SLUG = "AAAAAAAAAA";
    private static final String UPDATED_SLUG = "BBBBBBBBBB";

    private static final String DEFAULT_HTML = "AAAAAAAAAA";
    private static final String UPDATED_HTML = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_UPDATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_UPDATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_PUBLISHED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_PUBLISHED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final Status DEFAULT_STATUS = Status.PUBLISHED;
    private static final Status UPDATED_STATUS = Status.DRAFT;

    private static final Visibility DEFAULT_VISIBILITY = Visibility.PUBLIC;
    private static final Visibility UPDATED_VISIBILITY = Visibility.PRIVATE;

    private static final String ENTITY_API_URL = "/api/pages";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/pages/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private PagesRepository pagesRepository;

    @Mock
    private PagesRepository pagesRepositoryMock;

    @Autowired
    private PagesSearchRepository pagesSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPagesMockMvc;

    private Pages pages;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Pages createEntity(EntityManager em) {
        Pages pages = new Pages()
            .title(DEFAULT_TITLE)
            .slug(DEFAULT_SLUG)
            .html(DEFAULT_HTML)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT)
            .publishedAt(DEFAULT_PUBLISHED_AT)
            .status(DEFAULT_STATUS)
            .visibility(DEFAULT_VISIBILITY);
        return pages;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Pages createUpdatedEntity(EntityManager em) {
        Pages pages = new Pages()
            .title(UPDATED_TITLE)
            .slug(UPDATED_SLUG)
            .html(UPDATED_HTML)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .publishedAt(UPDATED_PUBLISHED_AT)
            .status(UPDATED_STATUS)
            .visibility(UPDATED_VISIBILITY);
        return pages;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        pagesSearchRepository.deleteAll();
        assertThat(pagesSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        pages = createEntity(em);
    }

    @Test
    @Transactional
    void createPages() throws Exception {
        int databaseSizeBeforeCreate = pagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pagesSearchRepository.findAll());
        // Create the Pages
        restPagesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(pages)))
            .andExpect(status().isCreated());

        // Validate the Pages in the database
        List<Pages> pagesList = pagesRepository.findAll();
        assertThat(pagesList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(pagesSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        Pages testPages = pagesList.get(pagesList.size() - 1);
        assertThat(testPages.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testPages.getSlug()).isEqualTo(DEFAULT_SLUG);
        assertThat(testPages.getHtml()).isEqualTo(DEFAULT_HTML);
        assertThat(testPages.getCreatedAt()).isEqualTo(DEFAULT_CREATED_AT);
        assertThat(testPages.getUpdatedAt()).isEqualTo(DEFAULT_UPDATED_AT);
        assertThat(testPages.getPublishedAt()).isEqualTo(DEFAULT_PUBLISHED_AT);
        assertThat(testPages.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testPages.getVisibility()).isEqualTo(DEFAULT_VISIBILITY);
    }

    @Test
    @Transactional
    void createPagesWithExistingId() throws Exception {
        // Create the Pages with an existing ID
        pages.setId(1L);

        int databaseSizeBeforeCreate = pagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pagesSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restPagesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(pages)))
            .andExpect(status().isBadRequest());

        // Validate the Pages in the database
        List<Pages> pagesList = pagesRepository.findAll();
        assertThat(pagesList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(pagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllPages() throws Exception {
        // Initialize the database
        pagesRepository.saveAndFlush(pages);

        // Get all the pagesList
        restPagesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pages.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].slug").value(hasItem(DEFAULT_SLUG)))
            .andExpect(jsonPath("$.[*].html").value(hasItem(DEFAULT_HTML.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(sameInstant(DEFAULT_CREATED_AT))))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(sameInstant(DEFAULT_UPDATED_AT))))
            .andExpect(jsonPath("$.[*].publishedAt").value(hasItem(sameInstant(DEFAULT_PUBLISHED_AT))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].visibility").value(hasItem(DEFAULT_VISIBILITY.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPagesWithEagerRelationshipsIsEnabled() throws Exception {
        when(pagesRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPagesMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(pagesRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPagesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(pagesRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPagesMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(pagesRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getPages() throws Exception {
        // Initialize the database
        pagesRepository.saveAndFlush(pages);

        // Get the pages
        restPagesMockMvc
            .perform(get(ENTITY_API_URL_ID, pages.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(pages.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.slug").value(DEFAULT_SLUG))
            .andExpect(jsonPath("$.html").value(DEFAULT_HTML.toString()))
            .andExpect(jsonPath("$.createdAt").value(sameInstant(DEFAULT_CREATED_AT)))
            .andExpect(jsonPath("$.updatedAt").value(sameInstant(DEFAULT_UPDATED_AT)))
            .andExpect(jsonPath("$.publishedAt").value(sameInstant(DEFAULT_PUBLISHED_AT)))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.visibility").value(DEFAULT_VISIBILITY.toString()));
    }

    @Test
    @Transactional
    void getNonExistingPages() throws Exception {
        // Get the pages
        restPagesMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPages() throws Exception {
        // Initialize the database
        pagesRepository.saveAndFlush(pages);

        int databaseSizeBeforeUpdate = pagesRepository.findAll().size();
        pagesSearchRepository.save(pages);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pagesSearchRepository.findAll());

        // Update the pages
        Pages updatedPages = pagesRepository.findById(pages.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPages are not directly saved in db
        em.detach(updatedPages);
        updatedPages
            .title(UPDATED_TITLE)
            .slug(UPDATED_SLUG)
            .html(UPDATED_HTML)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .publishedAt(UPDATED_PUBLISHED_AT)
            .status(UPDATED_STATUS)
            .visibility(UPDATED_VISIBILITY);

        restPagesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedPages.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedPages))
            )
            .andExpect(status().isOk());

        // Validate the Pages in the database
        List<Pages> pagesList = pagesRepository.findAll();
        assertThat(pagesList).hasSize(databaseSizeBeforeUpdate);
        Pages testPages = pagesList.get(pagesList.size() - 1);
        assertThat(testPages.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testPages.getSlug()).isEqualTo(UPDATED_SLUG);
        assertThat(testPages.getHtml()).isEqualTo(UPDATED_HTML);
        assertThat(testPages.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testPages.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
        assertThat(testPages.getPublishedAt()).isEqualTo(UPDATED_PUBLISHED_AT);
        assertThat(testPages.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testPages.getVisibility()).isEqualTo(UPDATED_VISIBILITY);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(pagesSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Pages> pagesSearchList = IterableUtils.toList(pagesSearchRepository.findAll());
                Pages testPagesSearch = pagesSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testPagesSearch.getTitle()).isEqualTo(UPDATED_TITLE);
                assertThat(testPagesSearch.getSlug()).isEqualTo(UPDATED_SLUG);
                assertThat(testPagesSearch.getHtml()).isEqualTo(UPDATED_HTML);
                assertThat(testPagesSearch.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
                assertThat(testPagesSearch.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
                assertThat(testPagesSearch.getPublishedAt()).isEqualTo(UPDATED_PUBLISHED_AT);
                assertThat(testPagesSearch.getStatus()).isEqualTo(UPDATED_STATUS);
                assertThat(testPagesSearch.getVisibility()).isEqualTo(UPDATED_VISIBILITY);
            });
    }

    @Test
    @Transactional
    void putNonExistingPages() throws Exception {
        int databaseSizeBeforeUpdate = pagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pagesSearchRepository.findAll());
        pages.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPagesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, pages.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(pages))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pages in the database
        List<Pages> pagesList = pagesRepository.findAll();
        assertThat(pagesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(pagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchPages() throws Exception {
        int databaseSizeBeforeUpdate = pagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pagesSearchRepository.findAll());
        pages.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPagesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(pages))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pages in the database
        List<Pages> pagesList = pagesRepository.findAll();
        assertThat(pagesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(pagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPages() throws Exception {
        int databaseSizeBeforeUpdate = pagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pagesSearchRepository.findAll());
        pages.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPagesMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(pages)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Pages in the database
        List<Pages> pagesList = pagesRepository.findAll();
        assertThat(pagesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(pagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdatePagesWithPatch() throws Exception {
        // Initialize the database
        pagesRepository.saveAndFlush(pages);

        int databaseSizeBeforeUpdate = pagesRepository.findAll().size();

        // Update the pages using partial update
        Pages partialUpdatedPages = new Pages();
        partialUpdatedPages.setId(pages.getId());

        partialUpdatedPages.title(UPDATED_TITLE).createdAt(UPDATED_CREATED_AT).updatedAt(UPDATED_UPDATED_AT).status(UPDATED_STATUS);

        restPagesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPages.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPages))
            )
            .andExpect(status().isOk());

        // Validate the Pages in the database
        List<Pages> pagesList = pagesRepository.findAll();
        assertThat(pagesList).hasSize(databaseSizeBeforeUpdate);
        Pages testPages = pagesList.get(pagesList.size() - 1);
        assertThat(testPages.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testPages.getSlug()).isEqualTo(DEFAULT_SLUG);
        assertThat(testPages.getHtml()).isEqualTo(DEFAULT_HTML);
        assertThat(testPages.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testPages.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
        assertThat(testPages.getPublishedAt()).isEqualTo(DEFAULT_PUBLISHED_AT);
        assertThat(testPages.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testPages.getVisibility()).isEqualTo(DEFAULT_VISIBILITY);
    }

    @Test
    @Transactional
    void fullUpdatePagesWithPatch() throws Exception {
        // Initialize the database
        pagesRepository.saveAndFlush(pages);

        int databaseSizeBeforeUpdate = pagesRepository.findAll().size();

        // Update the pages using partial update
        Pages partialUpdatedPages = new Pages();
        partialUpdatedPages.setId(pages.getId());

        partialUpdatedPages
            .title(UPDATED_TITLE)
            .slug(UPDATED_SLUG)
            .html(UPDATED_HTML)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .publishedAt(UPDATED_PUBLISHED_AT)
            .status(UPDATED_STATUS)
            .visibility(UPDATED_VISIBILITY);

        restPagesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPages.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPages))
            )
            .andExpect(status().isOk());

        // Validate the Pages in the database
        List<Pages> pagesList = pagesRepository.findAll();
        assertThat(pagesList).hasSize(databaseSizeBeforeUpdate);
        Pages testPages = pagesList.get(pagesList.size() - 1);
        assertThat(testPages.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testPages.getSlug()).isEqualTo(UPDATED_SLUG);
        assertThat(testPages.getHtml()).isEqualTo(UPDATED_HTML);
        assertThat(testPages.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testPages.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
        assertThat(testPages.getPublishedAt()).isEqualTo(UPDATED_PUBLISHED_AT);
        assertThat(testPages.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testPages.getVisibility()).isEqualTo(UPDATED_VISIBILITY);
    }

    @Test
    @Transactional
    void patchNonExistingPages() throws Exception {
        int databaseSizeBeforeUpdate = pagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pagesSearchRepository.findAll());
        pages.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPagesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, pages.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(pages))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pages in the database
        List<Pages> pagesList = pagesRepository.findAll();
        assertThat(pagesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(pagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPages() throws Exception {
        int databaseSizeBeforeUpdate = pagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pagesSearchRepository.findAll());
        pages.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPagesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(pages))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pages in the database
        List<Pages> pagesList = pagesRepository.findAll();
        assertThat(pagesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(pagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPages() throws Exception {
        int databaseSizeBeforeUpdate = pagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pagesSearchRepository.findAll());
        pages.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPagesMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(pages)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Pages in the database
        List<Pages> pagesList = pagesRepository.findAll();
        assertThat(pagesList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(pagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deletePages() throws Exception {
        // Initialize the database
        pagesRepository.saveAndFlush(pages);
        pagesRepository.save(pages);
        pagesSearchRepository.save(pages);

        int databaseSizeBeforeDelete = pagesRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(pagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the pages
        restPagesMockMvc
            .perform(delete(ENTITY_API_URL_ID, pages.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Pages> pagesList = pagesRepository.findAll();
        assertThat(pagesList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(pagesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchPages() throws Exception {
        // Initialize the database
        pages = pagesRepository.saveAndFlush(pages);
        pagesSearchRepository.save(pages);

        // Search the pages
        restPagesMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + pages.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pages.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].slug").value(hasItem(DEFAULT_SLUG)))
            .andExpect(jsonPath("$.[*].html").value(hasItem(DEFAULT_HTML.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(sameInstant(DEFAULT_CREATED_AT))))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(sameInstant(DEFAULT_UPDATED_AT))))
            .andExpect(jsonPath("$.[*].publishedAt").value(hasItem(sameInstant(DEFAULT_PUBLISHED_AT))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].visibility").value(hasItem(DEFAULT_VISIBILITY.toString())));
    }
}
