package org.nektototam.easyblog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nektototam.easyblog.domain.ItemTypesTestSamples.*;

import org.junit.jupiter.api.Test;
import org.nektototam.easyblog.web.rest.TestUtil;

class ItemTypesTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ItemTypes.class);
        ItemTypes itemTypes1 = getItemTypesSample1();
        ItemTypes itemTypes2 = new ItemTypes();
        assertThat(itemTypes1).isNotEqualTo(itemTypes2);

        itemTypes2.setId(itemTypes1.getId());
        assertThat(itemTypes1).isEqualTo(itemTypes2);

        itemTypes2 = getItemTypesSample2();
        assertThat(itemTypes1).isNotEqualTo(itemTypes2);
    }
}
