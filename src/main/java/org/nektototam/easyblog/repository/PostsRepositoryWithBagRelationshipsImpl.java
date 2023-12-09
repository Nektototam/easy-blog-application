package org.nektototam.easyblog.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.nektototam.easyblog.domain.Posts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class PostsRepositoryWithBagRelationshipsImpl implements PostsRepositoryWithBagRelationships {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Posts> fetchBagRelationships(Optional<Posts> posts) {
        return posts.map(this::fetchAuthors);
    }

    @Override
    public Page<Posts> fetchBagRelationships(Page<Posts> posts) {
        return new PageImpl<>(fetchBagRelationships(posts.getContent()), posts.getPageable(), posts.getTotalElements());
    }

    @Override
    public List<Posts> fetchBagRelationships(List<Posts> posts) {
        return Optional.of(posts).map(this::fetchAuthors).orElse(Collections.emptyList());
    }

    Posts fetchAuthors(Posts result) {
        return entityManager
            .createQuery("select posts from Posts posts left join fetch posts.authors where posts.id = :id", Posts.class)
            .setParameter("id", result.getId())
            .getSingleResult();
    }

    List<Posts> fetchAuthors(List<Posts> posts) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, posts.size()).forEach(index -> order.put(posts.get(index).getId(), index));
        List<Posts> result = entityManager
            .createQuery("select posts from Posts posts left join fetch posts.authors where posts in :posts", Posts.class)
            .setParameter("posts", posts)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
