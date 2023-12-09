package org.nektototam.easyblog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nektototam.easyblog.domain.PagesTestSamples.*;
import static org.nektototam.easyblog.domain.PostsTestSamples.*;
import static org.nektototam.easyblog.domain.UsersTestSamples.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.nektototam.easyblog.web.rest.TestUtil;

class UsersTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Users.class);
        Users users1 = getUsersSample1();
        Users users2 = new Users();
        assertThat(users1).isNotEqualTo(users2);

        users2.setId(users1.getId());
        assertThat(users1).isEqualTo(users2);

        users2 = getUsersSample2();
        assertThat(users1).isNotEqualTo(users2);
    }

    @Test
    void pageTest() throws Exception {
        Users users = getUsersRandomSampleGenerator();
        Pages pagesBack = getPagesRandomSampleGenerator();

        users.addPage(pagesBack);
        assertThat(users.getPages()).containsOnly(pagesBack);
        assertThat(pagesBack.getAuthors()).containsOnly(users);

        users.removePage(pagesBack);
        assertThat(users.getPages()).doesNotContain(pagesBack);
        assertThat(pagesBack.getAuthors()).doesNotContain(users);

        users.pages(new HashSet<>(Set.of(pagesBack)));
        assertThat(users.getPages()).containsOnly(pagesBack);
        assertThat(pagesBack.getAuthors()).containsOnly(users);

        users.setPages(new HashSet<>());
        assertThat(users.getPages()).doesNotContain(pagesBack);
        assertThat(pagesBack.getAuthors()).doesNotContain(users);
    }

    @Test
    void postTest() throws Exception {
        Users users = getUsersRandomSampleGenerator();
        Posts postsBack = getPostsRandomSampleGenerator();

        users.addPost(postsBack);
        assertThat(users.getPosts()).containsOnly(postsBack);
        assertThat(postsBack.getAuthors()).containsOnly(users);

        users.removePost(postsBack);
        assertThat(users.getPosts()).doesNotContain(postsBack);
        assertThat(postsBack.getAuthors()).doesNotContain(users);

        users.posts(new HashSet<>(Set.of(postsBack)));
        assertThat(users.getPosts()).containsOnly(postsBack);
        assertThat(postsBack.getAuthors()).containsOnly(users);

        users.setPosts(new HashSet<>());
        assertThat(users.getPosts()).doesNotContain(postsBack);
        assertThat(postsBack.getAuthors()).doesNotContain(users);
    }
}
