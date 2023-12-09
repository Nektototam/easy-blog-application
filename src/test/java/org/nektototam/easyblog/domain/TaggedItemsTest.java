package org.nektototam.easyblog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nektototam.easyblog.domain.ItemTypesTestSamples.*;
import static org.nektototam.easyblog.domain.PagesTestSamples.*;
import static org.nektototam.easyblog.domain.PostsTestSamples.*;
import static org.nektototam.easyblog.domain.TaggedItemsTestSamples.*;
import static org.nektototam.easyblog.domain.TagsTestSamples.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.nektototam.easyblog.web.rest.TestUtil;

class TaggedItemsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TaggedItems.class);
        TaggedItems taggedItems1 = getTaggedItemsSample1();
        TaggedItems taggedItems2 = new TaggedItems();
        assertThat(taggedItems1).isNotEqualTo(taggedItems2);

        taggedItems2.setId(taggedItems1.getId());
        assertThat(taggedItems1).isEqualTo(taggedItems2);

        taggedItems2 = getTaggedItemsSample2();
        assertThat(taggedItems1).isNotEqualTo(taggedItems2);
    }

    @Test
    void tagTest() throws Exception {
        TaggedItems taggedItems = getTaggedItemsRandomSampleGenerator();
        Tags tagsBack = getTagsRandomSampleGenerator();

        taggedItems.setTag(tagsBack);
        assertThat(taggedItems.getTag()).isEqualTo(tagsBack);

        taggedItems.tag(null);
        assertThat(taggedItems.getTag()).isNull();
    }

    @Test
    void itemTest() throws Exception {
        TaggedItems taggedItems = getTaggedItemsRandomSampleGenerator();
        ItemTypes itemTypesBack = getItemTypesRandomSampleGenerator();

        taggedItems.setItem(itemTypesBack);
        assertThat(taggedItems.getItem()).isEqualTo(itemTypesBack);

        taggedItems.item(null);
        assertThat(taggedItems.getItem()).isNull();
    }

    @Test
    void pageTest() throws Exception {
        TaggedItems taggedItems = getTaggedItemsRandomSampleGenerator();
        Pages pagesBack = getPagesRandomSampleGenerator();

        taggedItems.addPage(pagesBack);
        assertThat(taggedItems.getPages()).containsOnly(pagesBack);

        taggedItems.removePage(pagesBack);
        assertThat(taggedItems.getPages()).doesNotContain(pagesBack);

        taggedItems.pages(new HashSet<>(Set.of(pagesBack)));
        assertThat(taggedItems.getPages()).containsOnly(pagesBack);

        taggedItems.setPages(new HashSet<>());
        assertThat(taggedItems.getPages()).doesNotContain(pagesBack);
    }

    @Test
    void postTest() throws Exception {
        TaggedItems taggedItems = getTaggedItemsRandomSampleGenerator();
        Posts postsBack = getPostsRandomSampleGenerator();

        taggedItems.addPost(postsBack);
        assertThat(taggedItems.getPosts()).containsOnly(postsBack);

        taggedItems.removePost(postsBack);
        assertThat(taggedItems.getPosts()).doesNotContain(postsBack);

        taggedItems.posts(new HashSet<>(Set.of(postsBack)));
        assertThat(taggedItems.getPosts()).containsOnly(postsBack);

        taggedItems.setPosts(new HashSet<>());
        assertThat(taggedItems.getPosts()).doesNotContain(postsBack);
    }
}
