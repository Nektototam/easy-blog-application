package org.nektototam.easyblog.repository;

import java.util.List;
import java.util.Optional;
import org.nektototam.easyblog.domain.Posts;
import org.springframework.data.domain.Page;

public interface PostsRepositoryWithBagRelationships {
    Optional<Posts> fetchBagRelationships(Optional<Posts> posts);

    List<Posts> fetchBagRelationships(List<Posts> posts);

    Page<Posts> fetchBagRelationships(Page<Posts> posts);
}
