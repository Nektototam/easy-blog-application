package org.nektototam.easyblog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nektototam.easyblog.domain.AuthorsTestSamples.*;
import static org.nektototam.easyblog.domain.CommentsTestSamples.*;
import static org.nektototam.easyblog.domain.CommentsTestSamples.*;
import static org.nektototam.easyblog.domain.PagesTestSamples.*;
import static org.nektototam.easyblog.domain.PostsTestSamples.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.nektototam.easyblog.web.rest.TestUtil;

class CommentsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Comments.class);
        Comments comments1 = getCommentsSample1();
        Comments comments2 = new Comments();
        assertThat(comments1).isNotEqualTo(comments2);

        comments2.setId(comments1.getId());
        assertThat(comments1).isEqualTo(comments2);

        comments2 = getCommentsSample2();
        assertThat(comments1).isNotEqualTo(comments2);
    }

    @Test
    void authorTest() throws Exception {
        Comments comments = getCommentsRandomSampleGenerator();
        Authors authorsBack = getAuthorsRandomSampleGenerator();

        comments.setAuthor(authorsBack);
        assertThat(comments.getAuthor()).isEqualTo(authorsBack);

        comments.author(null);
        assertThat(comments.getAuthor()).isNull();
    }

    @Test
    void postTest() throws Exception {
        Comments comments = getCommentsRandomSampleGenerator();
        Posts postsBack = getPostsRandomSampleGenerator();

        comments.addPost(postsBack);
        assertThat(comments.getPosts()).containsOnly(postsBack);

        comments.removePost(postsBack);
        assertThat(comments.getPosts()).doesNotContain(postsBack);

        comments.posts(new HashSet<>(Set.of(postsBack)));
        assertThat(comments.getPosts()).containsOnly(postsBack);

        comments.setPosts(new HashSet<>());
        assertThat(comments.getPosts()).doesNotContain(postsBack);
    }

    @Test
    void pageTest() throws Exception {
        Comments comments = getCommentsRandomSampleGenerator();
        Pages pagesBack = getPagesRandomSampleGenerator();

        comments.addPage(pagesBack);
        assertThat(comments.getPages()).containsOnly(pagesBack);

        comments.removePage(pagesBack);
        assertThat(comments.getPages()).doesNotContain(pagesBack);

        comments.pages(new HashSet<>(Set.of(pagesBack)));
        assertThat(comments.getPages()).containsOnly(pagesBack);

        comments.setPages(new HashSet<>());
        assertThat(comments.getPages()).doesNotContain(pagesBack);
    }

    @Test
    void parentTest() throws Exception {
        Comments comments = getCommentsRandomSampleGenerator();
        Comments commentsBack = getCommentsRandomSampleGenerator();

        comments.addParent(commentsBack);
        assertThat(comments.getParents()).containsOnly(commentsBack);

        comments.removeParent(commentsBack);
        assertThat(comments.getParents()).doesNotContain(commentsBack);

        comments.parents(new HashSet<>(Set.of(commentsBack)));
        assertThat(comments.getParents()).containsOnly(commentsBack);

        comments.setParents(new HashSet<>());
        assertThat(comments.getParents()).doesNotContain(commentsBack);
    }

    @Test
    void childTest() throws Exception {
        Comments comments = getCommentsRandomSampleGenerator();
        Comments commentsBack = getCommentsRandomSampleGenerator();

        comments.addChild(commentsBack);
        assertThat(comments.getChildren()).containsOnly(commentsBack);
        assertThat(commentsBack.getParents()).containsOnly(comments);

        comments.removeChild(commentsBack);
        assertThat(comments.getChildren()).doesNotContain(commentsBack);
        assertThat(commentsBack.getParents()).doesNotContain(comments);

        comments.children(new HashSet<>(Set.of(commentsBack)));
        assertThat(comments.getChildren()).containsOnly(commentsBack);
        assertThat(commentsBack.getParents()).containsOnly(comments);

        comments.setChildren(new HashSet<>());
        assertThat(comments.getChildren()).doesNotContain(commentsBack);
        assertThat(commentsBack.getParents()).doesNotContain(comments);
    }
}
