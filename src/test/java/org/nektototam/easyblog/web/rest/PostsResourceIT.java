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
import org.nektototam.easyblog.domain.Posts;
import org.nektototam.easyblog.domain.enumeration.Status;
import org.nektototam.easyblog.domain.enumeration.Visibility;
import org.nektototam.easyblog.repository.PostsRepository;
import org.nektototam.easyblog.repository.search.PostsSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link PostsResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class PostsResourceIT {

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

    private static final String ENTITY_API_URL = "/api/posts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/posts/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private PostsRepository postsRepository;

    @Mock
    private PostsRepository postsRepositoryMock;

    @Autowired
    private PostsSearchRepository postsSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPostsMockMvc;

    private Posts posts;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Posts createEntity(EntityManager em) {
        Posts posts = new Posts()
            .title(DEFAULT_TITLE)
            .slug(DEFAULT_SLUG)
            .html(DEFAULT_HTML)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT)
            .publishedAt(DEFAULT_PUBLISHED_AT)
            .status(DEFAULT_STATUS)
            .visibility(DEFAULT_VISIBILITY);
        return posts;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Posts createUpdatedEntity(EntityManager em) {
        Posts posts = new Posts()
            .title(UPDATED_TITLE)
            .slug(UPDATED_SLUG)
            .html(UPDATED_HTML)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .publishedAt(UPDATED_PUBLISHED_AT)
            .status(UPDATED_STATUS)
            .visibility(UPDATED_VISIBILITY);
        return posts;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        postsSearchRepository.deleteAll();
        assertThat(postsSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        posts = createEntity(em);
    }

    @Test
    @Transactional
    void createPosts() throws Exception {
        int databaseSizeBeforeCreate = postsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(postsSearchRepository.findAll());
        // Create the Posts
        restPostsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(posts)))
            .andExpect(status().isCreated());

        // Validate the Posts in the database
        List<Posts> postsList = postsRepository.findAll();
        assertThat(postsList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(postsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        Posts testPosts = postsList.get(postsList.size() - 1);
        assertThat(testPosts.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testPosts.getSlug()).isEqualTo(DEFAULT_SLUG);
        assertThat(testPosts.getHtml()).isEqualTo(DEFAULT_HTML);
        assertThat(testPosts.getCreatedAt()).isEqualTo(DEFAULT_CREATED_AT);
        assertThat(testPosts.getUpdatedAt()).isEqualTo(DEFAULT_UPDATED_AT);
        assertThat(testPosts.getPublishedAt()).isEqualTo(DEFAULT_PUBLISHED_AT);
        assertThat(testPosts.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testPosts.getVisibility()).isEqualTo(DEFAULT_VISIBILITY);
    }

    @Test
    @Transactional
    void createPostsWithExistingId() throws Exception {
        // Create the Posts with an existing ID
        posts.setId(1L);

        int databaseSizeBeforeCreate = postsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(postsSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restPostsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(posts)))
            .andExpect(status().isBadRequest());

        // Validate the Posts in the database
        List<Posts> postsList = postsRepository.findAll();
        assertThat(postsList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(postsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllPosts() throws Exception {
        // Initialize the database
        postsRepository.saveAndFlush(posts);

        // Get all the postsList
        restPostsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(posts.getId().intValue())))
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
    void getAllPostsWithEagerRelationshipsIsEnabled() throws Exception {
        when(postsRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPostsMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(postsRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPostsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(postsRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPostsMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(postsRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getPosts() throws Exception {
        // Initialize the database
        postsRepository.saveAndFlush(posts);

        // Get the posts
        restPostsMockMvc
            .perform(get(ENTITY_API_URL_ID, posts.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(posts.getId().intValue()))
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
    void getNonExistingPosts() throws Exception {
        // Get the posts
        restPostsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPosts() throws Exception {
        // Initialize the database
        postsRepository.saveAndFlush(posts);

        int databaseSizeBeforeUpdate = postsRepository.findAll().size();
        postsSearchRepository.save(posts);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(postsSearchRepository.findAll());

        // Update the posts
        Posts updatedPosts = postsRepository.findById(posts.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPosts are not directly saved in db
        em.detach(updatedPosts);
        updatedPosts
            .title(UPDATED_TITLE)
            .slug(UPDATED_SLUG)
            .html(UPDATED_HTML)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .publishedAt(UPDATED_PUBLISHED_AT)
            .status(UPDATED_STATUS)
            .visibility(UPDATED_VISIBILITY);

        restPostsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedPosts.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedPosts))
            )
            .andExpect(status().isOk());

        // Validate the Posts in the database
        List<Posts> postsList = postsRepository.findAll();
        assertThat(postsList).hasSize(databaseSizeBeforeUpdate);
        Posts testPosts = postsList.get(postsList.size() - 1);
        assertThat(testPosts.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testPosts.getSlug()).isEqualTo(UPDATED_SLUG);
        assertThat(testPosts.getHtml()).isEqualTo(UPDATED_HTML);
        assertThat(testPosts.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testPosts.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
        assertThat(testPosts.getPublishedAt()).isEqualTo(UPDATED_PUBLISHED_AT);
        assertThat(testPosts.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testPosts.getVisibility()).isEqualTo(UPDATED_VISIBILITY);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(postsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Posts> postsSearchList = IterableUtils.toList(postsSearchRepository.findAll());
                Posts testPostsSearch = postsSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testPostsSearch.getTitle()).isEqualTo(UPDATED_TITLE);
                assertThat(testPostsSearch.getSlug()).isEqualTo(UPDATED_SLUG);
                assertThat(testPostsSearch.getHtml()).isEqualTo(UPDATED_HTML);
                assertThat(testPostsSearch.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
                assertThat(testPostsSearch.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
                assertThat(testPostsSearch.getPublishedAt()).isEqualTo(UPDATED_PUBLISHED_AT);
                assertThat(testPostsSearch.getStatus()).isEqualTo(UPDATED_STATUS);
                assertThat(testPostsSearch.getVisibility()).isEqualTo(UPDATED_VISIBILITY);
            });
    }

    @Test
    @Transactional
    void putNonExistingPosts() throws Exception {
        int databaseSizeBeforeUpdate = postsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(postsSearchRepository.findAll());
        posts.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPostsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, posts.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(posts))
            )
            .andExpect(status().isBadRequest());

        // Validate the Posts in the database
        List<Posts> postsList = postsRepository.findAll();
        assertThat(postsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(postsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchPosts() throws Exception {
        int databaseSizeBeforeUpdate = postsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(postsSearchRepository.findAll());
        posts.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPostsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(posts))
            )
            .andExpect(status().isBadRequest());

        // Validate the Posts in the database
        List<Posts> postsList = postsRepository.findAll();
        assertThat(postsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(postsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPosts() throws Exception {
        int databaseSizeBeforeUpdate = postsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(postsSearchRepository.findAll());
        posts.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPostsMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(posts)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Posts in the database
        List<Posts> postsList = postsRepository.findAll();
        assertThat(postsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(postsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdatePostsWithPatch() throws Exception {
        // Initialize the database
        postsRepository.saveAndFlush(posts);

        int databaseSizeBeforeUpdate = postsRepository.findAll().size();

        // Update the posts using partial update
        Posts partialUpdatedPosts = new Posts();
        partialUpdatedPosts.setId(posts.getId());

        partialUpdatedPosts.createdAt(UPDATED_CREATED_AT).updatedAt(UPDATED_UPDATED_AT).visibility(UPDATED_VISIBILITY);

        restPostsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPosts.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPosts))
            )
            .andExpect(status().isOk());

        // Validate the Posts in the database
        List<Posts> postsList = postsRepository.findAll();
        assertThat(postsList).hasSize(databaseSizeBeforeUpdate);
        Posts testPosts = postsList.get(postsList.size() - 1);
        assertThat(testPosts.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testPosts.getSlug()).isEqualTo(DEFAULT_SLUG);
        assertThat(testPosts.getHtml()).isEqualTo(DEFAULT_HTML);
        assertThat(testPosts.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testPosts.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
        assertThat(testPosts.getPublishedAt()).isEqualTo(DEFAULT_PUBLISHED_AT);
        assertThat(testPosts.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testPosts.getVisibility()).isEqualTo(UPDATED_VISIBILITY);
    }

    @Test
    @Transactional
    void fullUpdatePostsWithPatch() throws Exception {
        // Initialize the database
        postsRepository.saveAndFlush(posts);

        int databaseSizeBeforeUpdate = postsRepository.findAll().size();

        // Update the posts using partial update
        Posts partialUpdatedPosts = new Posts();
        partialUpdatedPosts.setId(posts.getId());

        partialUpdatedPosts
            .title(UPDATED_TITLE)
            .slug(UPDATED_SLUG)
            .html(UPDATED_HTML)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .publishedAt(UPDATED_PUBLISHED_AT)
            .status(UPDATED_STATUS)
            .visibility(UPDATED_VISIBILITY);

        restPostsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPosts.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPosts))
            )
            .andExpect(status().isOk());

        // Validate the Posts in the database
        List<Posts> postsList = postsRepository.findAll();
        assertThat(postsList).hasSize(databaseSizeBeforeUpdate);
        Posts testPosts = postsList.get(postsList.size() - 1);
        assertThat(testPosts.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testPosts.getSlug()).isEqualTo(UPDATED_SLUG);
        assertThat(testPosts.getHtml()).isEqualTo(UPDATED_HTML);
        assertThat(testPosts.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testPosts.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
        assertThat(testPosts.getPublishedAt()).isEqualTo(UPDATED_PUBLISHED_AT);
        assertThat(testPosts.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testPosts.getVisibility()).isEqualTo(UPDATED_VISIBILITY);
    }

    @Test
    @Transactional
    void patchNonExistingPosts() throws Exception {
        int databaseSizeBeforeUpdate = postsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(postsSearchRepository.findAll());
        posts.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPostsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, posts.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(posts))
            )
            .andExpect(status().isBadRequest());

        // Validate the Posts in the database
        List<Posts> postsList = postsRepository.findAll();
        assertThat(postsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(postsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPosts() throws Exception {
        int databaseSizeBeforeUpdate = postsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(postsSearchRepository.findAll());
        posts.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPostsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(posts))
            )
            .andExpect(status().isBadRequest());

        // Validate the Posts in the database
        List<Posts> postsList = postsRepository.findAll();
        assertThat(postsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(postsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPosts() throws Exception {
        int databaseSizeBeforeUpdate = postsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(postsSearchRepository.findAll());
        posts.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPostsMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(posts)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Posts in the database
        List<Posts> postsList = postsRepository.findAll();
        assertThat(postsList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(postsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deletePosts() throws Exception {
        // Initialize the database
        postsRepository.saveAndFlush(posts);
        postsRepository.save(posts);
        postsSearchRepository.save(posts);

        int databaseSizeBeforeDelete = postsRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(postsSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the posts
        restPostsMockMvc
            .perform(delete(ENTITY_API_URL_ID, posts.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Posts> postsList = postsRepository.findAll();
        assertThat(postsList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(postsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchPosts() throws Exception {
        // Initialize the database
        posts = postsRepository.saveAndFlush(posts);
        postsSearchRepository.save(posts);

        // Search the posts
        restPostsMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + posts.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(posts.getId().intValue())))
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
