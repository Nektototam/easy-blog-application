package org.nektototam.easyblog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nektototam.easyblog.domain.CommentsTestSamples.*;
import static org.nektototam.easyblog.domain.PostsTestSamples.*;
import static org.nektototam.easyblog.domain.TaggedItemsTestSamples.*;
import static org.nektototam.easyblog.domain.UsersTestSamples.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.nektototam.easyblog.web.rest.TestUtil;

class PostsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Posts.class);
        Posts posts1 = getPostsSample1();
        Posts posts2 = new Posts();
        assertThat(posts1).isNotEqualTo(posts2);

        posts2.setId(posts1.getId());
        assertThat(posts1).isEqualTo(posts2);

        posts2 = getPostsSample2();
        assertThat(posts1).isNotEqualTo(posts2);
    }

    @Test
    void authorTest() throws Exception {
        Posts posts = getPostsRandomSampleGenerator();
        Users usersBack = getUsersRandomSampleGenerator();

        posts.addAuthor(usersBack);
        assertThat(posts.getAuthors()).containsOnly(usersBack);

        posts.removeAuthor(usersBack);
        assertThat(posts.getAuthors()).doesNotContain(usersBack);

        posts.authors(new HashSet<>(Set.of(usersBack)));
        assertThat(posts.getAuthors()).containsOnly(usersBack);

        posts.setAuthors(new HashSet<>());
        assertThat(posts.getAuthors()).doesNotContain(usersBack);
    }

    @Test
    void tagTest() throws Exception {
        Posts posts = getPostsRandomSampleGenerator();
        TaggedItems taggedItemsBack = getTaggedItemsRandomSampleGenerator();

        posts.addTag(taggedItemsBack);
        assertThat(posts.getTags()).containsOnly(taggedItemsBack);
        assertThat(taggedItemsBack.getPosts()).containsOnly(posts);

        posts.removeTag(taggedItemsBack);
        assertThat(posts.getTags()).doesNotContain(taggedItemsBack);
        assertThat(taggedItemsBack.getPosts()).doesNotContain(posts);

        posts.tags(new HashSet<>(Set.of(taggedItemsBack)));
        assertThat(posts.getTags()).containsOnly(taggedItemsBack);
        assertThat(taggedItemsBack.getPosts()).containsOnly(posts);

        posts.setTags(new HashSet<>());
        assertThat(posts.getTags()).doesNotContain(taggedItemsBack);
        assertThat(taggedItemsBack.getPosts()).doesNotContain(posts);
    }

    @Test
    void commentTest() throws Exception {
        Posts posts = getPostsRandomSampleGenerator();
        Comments commentsBack = getCommentsRandomSampleGenerator();

        posts.addComment(commentsBack);
        assertThat(posts.getComments()).containsOnly(commentsBack);
        assertThat(commentsBack.getPosts()).containsOnly(posts);

        posts.removeComment(commentsBack);
        assertThat(posts.getComments()).doesNotContain(commentsBack);
        assertThat(commentsBack.getPosts()).doesNotContain(posts);

        posts.comments(new HashSet<>(Set.of(commentsBack)));
        assertThat(posts.getComments()).containsOnly(commentsBack);
        assertThat(commentsBack.getPosts()).containsOnly(posts);

        posts.setComments(new HashSet<>());
        assertThat(posts.getComments()).doesNotContain(commentsBack);
        assertThat(commentsBack.getPosts()).doesNotContain(posts);
    }
}
