package org.nektototam.easyblog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nektototam.easyblog.domain.AuthorsTestSamples.*;

import org.junit.jupiter.api.Test;
import org.nektototam.easyblog.web.rest.TestUtil;

class AuthorsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Authors.class);
        Authors authors1 = getAuthorsSample1();
        Authors authors2 = new Authors();
        assertThat(authors1).isNotEqualTo(authors2);

        authors2.setId(authors1.getId());
        assertThat(authors1).isEqualTo(authors2);

        authors2 = getAuthorsSample2();
        assertThat(authors1).isNotEqualTo(authors2);
    }
}
