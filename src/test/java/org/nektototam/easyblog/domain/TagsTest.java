package org.nektototam.easyblog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nektototam.easyblog.domain.TagsTestSamples.*;

import org.junit.jupiter.api.Test;
import org.nektototam.easyblog.web.rest.TestUtil;

class TagsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Tags.class);
        Tags tags1 = getTagsSample1();
        Tags tags2 = new Tags();
        assertThat(tags1).isNotEqualTo(tags2);

        tags2.setId(tags1.getId());
        assertThat(tags1).isEqualTo(tags2);

        tags2 = getTagsSample2();
        assertThat(tags1).isNotEqualTo(tags2);
    }
}
