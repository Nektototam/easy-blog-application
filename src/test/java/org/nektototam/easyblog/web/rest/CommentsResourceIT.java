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
import org.nektototam.easyblog.domain.Comments;
import org.nektototam.easyblog.domain.enumeration.Status;
import org.nektototam.easyblog.repository.CommentsRepository;
import org.nektototam.easyblog.repository.search.CommentsSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link CommentsResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class CommentsResourceIT {

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_UPDATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_UPDATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final Status DEFAULT_STATUS = Status.PUBLISHED;
    private static final Status UPDATED_STATUS = Status.DRAFT;

    private static final String ENTITY_API_URL = "/api/comments";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/comments/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CommentsRepository commentsRepository;

    @Mock
    private CommentsRepository commentsRepositoryMock;

    @Autowired
    private CommentsSearchRepository commentsSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCommentsMockMvc;

    private Comments comments;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Comments createEntity(EntityManager em) {
        Comments comments = new Comments()
            .content(DEFAULT_CONTENT)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT)
            .status(DEFAULT_STATUS);
        return comments;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Comments createUpdatedEntity(EntityManager em) {
        Comments comments = new Comments()
            .content(UPDATED_CONTENT)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .status(UPDATED_STATUS);
        return comments;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        commentsSearchRepository.deleteAll();
        assertThat(commentsSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        comments = createEntity(em);
    }

    @Test
    @Transactional
    void createComments() throws Exception {
        int databaseSizeBeforeCreate = commentsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        // Create the Comments
        restCommentsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(comments)))
            .andExpect(status().isCreated());

        // Validate the Comments in the database
        List<Comments> commentsList = commentsRepository.findAll();
        assertThat(commentsList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(commentsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        Comments testComments = commentsList.get(commentsList.size() - 1);
        assertThat(testComments.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testComments.getCreatedAt()).isEqualTo(DEFAULT_CREATED_AT);
        assertThat(testComments.getUpdatedAt()).isEqualTo(DEFAULT_UPDATED_AT);
        assertThat(testComments.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    @Transactional
    void createCommentsWithExistingId() throws Exception {
        // Create the Comments with an existing ID
        comments.setId(1L);

        int databaseSizeBeforeCreate = commentsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(commentsSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restCommentsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(comments)))
            .andExpect(status().isBadRequest());

        // Validate the Comments in the database
        List<Comments> commentsList = commentsRepository.findAll();
        assertThat(commentsList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        int databaseSizeBeforeTest = commentsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        // set the field null
        comments.setCreatedAt(null);

        // Create the Comments, which fails.

        restCommentsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(comments)))
            .andExpect(status().isBadRequest());

        List<Comments> commentsList = commentsRepository.findAll();
        assertThat(commentsList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkUpdatedAtIsRequired() throws Exception {
        int databaseSizeBeforeTest = commentsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        // set the field null
        comments.setUpdatedAt(null);

        // Create the Comments, which fails.

        restCommentsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(comments)))
            .andExpect(status().isBadRequest());

        List<Comments> commentsList = commentsRepository.findAll();
        assertThat(commentsList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllComments() throws Exception {
        // Initialize the database
        commentsRepository.saveAndFlush(comments);

        // Get all the commentsList
        restCommentsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(comments.getId().intValue())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(sameInstant(DEFAULT_CREATED_AT))))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(sameInstant(DEFAULT_UPDATED_AT))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCommentsWithEagerRelationshipsIsEnabled() throws Exception {
        when(commentsRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCommentsMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(commentsRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCommentsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(commentsRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCommentsMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(commentsRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getComments() throws Exception {
        // Initialize the database
        commentsRepository.saveAndFlush(comments);

        // Get the comments
        restCommentsMockMvc
            .perform(get(ENTITY_API_URL_ID, comments.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(comments.getId().intValue()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT.toString()))
            .andExpect(jsonPath("$.createdAt").value(sameInstant(DEFAULT_CREATED_AT)))
            .andExpect(jsonPath("$.updatedAt").value(sameInstant(DEFAULT_UPDATED_AT)))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    void getNonExistingComments() throws Exception {
        // Get the comments
        restCommentsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingComments() throws Exception {
        // Initialize the database
        commentsRepository.saveAndFlush(comments);

        int databaseSizeBeforeUpdate = commentsRepository.findAll().size();
        commentsSearchRepository.save(comments);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(commentsSearchRepository.findAll());

        // Update the comments
        Comments updatedComments = commentsRepository.findById(comments.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedComments are not directly saved in db
        em.detach(updatedComments);
        updatedComments.content(UPDATED_CONTENT).createdAt(UPDATED_CREATED_AT).updatedAt(UPDATED_UPDATED_AT).status(UPDATED_STATUS);

        restCommentsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedComments.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedComments))
            )
            .andExpect(status().isOk());

        // Validate the Comments in the database
        List<Comments> commentsList = commentsRepository.findAll();
        assertThat(commentsList).hasSize(databaseSizeBeforeUpdate);
        Comments testComments = commentsList.get(commentsList.size() - 1);
        assertThat(testComments.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testComments.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testComments.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
        assertThat(testComments.getStatus()).isEqualTo(UPDATED_STATUS);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(commentsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Comments> commentsSearchList = IterableUtils.toList(commentsSearchRepository.findAll());
                Comments testCommentsSearch = commentsSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testCommentsSearch.getContent()).isEqualTo(UPDATED_CONTENT);
                assertThat(testCommentsSearch.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
                assertThat(testCommentsSearch.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
                assertThat(testCommentsSearch.getStatus()).isEqualTo(UPDATED_STATUS);
            });
    }

    @Test
    @Transactional
    void putNonExistingComments() throws Exception {
        int databaseSizeBeforeUpdate = commentsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        comments.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCommentsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, comments.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(comments))
            )
            .andExpect(status().isBadRequest());

        // Validate the Comments in the database
        List<Comments> commentsList = commentsRepository.findAll();
        assertThat(commentsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchComments() throws Exception {
        int databaseSizeBeforeUpdate = commentsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        comments.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommentsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(comments))
            )
            .andExpect(status().isBadRequest());

        // Validate the Comments in the database
        List<Comments> commentsList = commentsRepository.findAll();
        assertThat(commentsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamComments() throws Exception {
        int databaseSizeBeforeUpdate = commentsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        comments.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommentsMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(comments)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Comments in the database
        List<Comments> commentsList = commentsRepository.findAll();
        assertThat(commentsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateCommentsWithPatch() throws Exception {
        // Initialize the database
        commentsRepository.saveAndFlush(comments);

        int databaseSizeBeforeUpdate = commentsRepository.findAll().size();

        // Update the comments using partial update
        Comments partialUpdatedComments = new Comments();
        partialUpdatedComments.setId(comments.getId());

        restCommentsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedComments.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedComments))
            )
            .andExpect(status().isOk());

        // Validate the Comments in the database
        List<Comments> commentsList = commentsRepository.findAll();
        assertThat(commentsList).hasSize(databaseSizeBeforeUpdate);
        Comments testComments = commentsList.get(commentsList.size() - 1);
        assertThat(testComments.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testComments.getCreatedAt()).isEqualTo(DEFAULT_CREATED_AT);
        assertThat(testComments.getUpdatedAt()).isEqualTo(DEFAULT_UPDATED_AT);
        assertThat(testComments.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    @Transactional
    void fullUpdateCommentsWithPatch() throws Exception {
        // Initialize the database
        commentsRepository.saveAndFlush(comments);

        int databaseSizeBeforeUpdate = commentsRepository.findAll().size();

        // Update the comments using partial update
        Comments partialUpdatedComments = new Comments();
        partialUpdatedComments.setId(comments.getId());

        partialUpdatedComments.content(UPDATED_CONTENT).createdAt(UPDATED_CREATED_AT).updatedAt(UPDATED_UPDATED_AT).status(UPDATED_STATUS);

        restCommentsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedComments.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedComments))
            )
            .andExpect(status().isOk());

        // Validate the Comments in the database
        List<Comments> commentsList = commentsRepository.findAll();
        assertThat(commentsList).hasSize(databaseSizeBeforeUpdate);
        Comments testComments = commentsList.get(commentsList.size() - 1);
        assertThat(testComments.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testComments.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testComments.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
        assertThat(testComments.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @Transactional
    void patchNonExistingComments() throws Exception {
        int databaseSizeBeforeUpdate = commentsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        comments.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCommentsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, comments.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(comments))
            )
            .andExpect(status().isBadRequest());

        // Validate the Comments in the database
        List<Comments> commentsList = commentsRepository.findAll();
        assertThat(commentsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchComments() throws Exception {
        int databaseSizeBeforeUpdate = commentsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        comments.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommentsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(comments))
            )
            .andExpect(status().isBadRequest());

        // Validate the Comments in the database
        List<Comments> commentsList = commentsRepository.findAll();
        assertThat(commentsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamComments() throws Exception {
        int databaseSizeBeforeUpdate = commentsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        comments.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommentsMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(comments)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Comments in the database
        List<Comments> commentsList = commentsRepository.findAll();
        assertThat(commentsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteComments() throws Exception {
        // Initialize the database
        commentsRepository.saveAndFlush(comments);
        commentsRepository.save(comments);
        commentsSearchRepository.save(comments);

        int databaseSizeBeforeDelete = commentsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the comments
        restCommentsMockMvc
            .perform(delete(ENTITY_API_URL_ID, comments.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Comments> commentsList = commentsRepository.findAll();
        assertThat(commentsList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(commentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchComments() throws Exception {
        // Initialize the database
        comments = commentsRepository.saveAndFlush(comments);
        commentsSearchRepository.save(comments);

        // Search the comments
        restCommentsMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + comments.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(comments.getId().intValue())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(sameInstant(DEFAULT_CREATED_AT))))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(sameInstant(DEFAULT_UPDATED_AT))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }
}
