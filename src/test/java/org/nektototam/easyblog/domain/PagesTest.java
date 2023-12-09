package org.nektototam.easyblog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nektototam.easyblog.domain.CommentsTestSamples.*;
import static org.nektototam.easyblog.domain.PagesTestSamples.*;
import static org.nektototam.easyblog.domain.TaggedItemsTestSamples.*;
import static org.nektototam.easyblog.domain.UsersTestSamples.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.nektototam.easyblog.web.rest.TestUtil;

class PagesTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Pages.class);
        Pages pages1 = getPagesSample1();
        Pages pages2 = new Pages();
        assertThat(pages1).isNotEqualTo(pages2);

        pages2.setId(pages1.getId());
        assertThat(pages1).isEqualTo(pages2);

        pages2 = getPagesSample2();
        assertThat(pages1).isNotEqualTo(pages2);
    }

    @Test
    void authorTest() throws Exception {
        Pages pages = getPagesRandomSampleGenerator();
        Users usersBack = getUsersRandomSampleGenerator();

        pages.addAuthor(usersBack);
        assertThat(pages.getAuthors()).containsOnly(usersBack);

        pages.removeAuthor(usersBack);
        assertThat(pages.getAuthors()).doesNotContain(usersBack);

        pages.authors(new HashSet<>(Set.of(usersBack)));
        assertThat(pages.getAuthors()).containsOnly(usersBack);

        pages.setAuthors(new HashSet<>());
        assertThat(pages.getAuthors()).doesNotContain(usersBack);
    }

    @Test
    void tagTest() throws Exception {
        Pages pages = getPagesRandomSampleGenerator();
        TaggedItems taggedItemsBack = getTaggedItemsRandomSampleGenerator();

        pages.addTag(taggedItemsBack);
        assertThat(pages.getTags()).containsOnly(taggedItemsBack);
        assertThat(taggedItemsBack.getPages()).containsOnly(pages);

        pages.removeTag(taggedItemsBack);
        assertThat(pages.getTags()).doesNotContain(taggedItemsBack);
        assertThat(taggedItemsBack.getPages()).doesNotContain(pages);

        pages.tags(new HashSet<>(Set.of(taggedItemsBack)));
        assertThat(pages.getTags()).containsOnly(taggedItemsBack);
        assertThat(taggedItemsBack.getPages()).containsOnly(pages);

        pages.setTags(new HashSet<>());
        assertThat(pages.getTags()).doesNotContain(taggedItemsBack);
        assertThat(taggedItemsBack.getPages()).doesNotContain(pages);
    }

    @Test
    void commentTest() throws Exception {
        Pages pages = getPagesRandomSampleGenerator();
        Comments commentsBack = getCommentsRandomSampleGenerator();

        pages.addComment(commentsBack);
        assertThat(pages.getComments()).containsOnly(commentsBack);
        assertThat(commentsBack.getPages()).containsOnly(pages);

        pages.removeComment(commentsBack);
        assertThat(pages.getComments()).doesNotContain(commentsBack);
        assertThat(commentsBack.getPages()).doesNotContain(pages);

        pages.comments(new HashSet<>(Set.of(commentsBack)));
        assertThat(pages.getComments()).containsOnly(commentsBack);
        assertThat(commentsBack.getPages()).containsOnly(pages);

        pages.setComments(new HashSet<>());
        assertThat(pages.getComments()).doesNotContain(commentsBack);
        assertThat(commentsBack.getPages()).doesNotContain(pages);
    }
}
