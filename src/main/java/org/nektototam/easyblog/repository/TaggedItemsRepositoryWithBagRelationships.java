package org.nektototam.easyblog.repository;

import java.util.List;
import java.util.Optional;
import org.nektototam.easyblog.domain.TaggedItems;
import org.springframework.data.domain.Page;

public interface TaggedItemsRepositoryWithBagRelationships {
    Optional<TaggedItems> fetchBagRelationships(Optional<TaggedItems> taggedItems);

    List<TaggedItems> fetchBagRelationships(List<TaggedItems> taggedItems);

    Page<TaggedItems> fetchBagRelationships(Page<TaggedItems> taggedItems);
}
