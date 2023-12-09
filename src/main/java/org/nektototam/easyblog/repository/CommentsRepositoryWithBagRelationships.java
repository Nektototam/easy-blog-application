package org.nektototam.easyblog.repository;

import java.util.List;
import java.util.Optional;
import org.nektototam.easyblog.domain.Comments;
import org.springframework.data.domain.Page;

public interface CommentsRepositoryWithBagRelationships {
    Optional<Comments> fetchBagRelationships(Optional<Comments> comments);

    List<Comments> fetchBagRelationships(List<Comments> comments);

    Page<Comments> fetchBagRelationships(Page<Comments> comments);
}
