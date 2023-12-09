package org.nektototam.easyblog.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.nektototam.easyblog.domain.TaggedItems;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class TaggedItemsRepositoryWithBagRelationshipsImpl implements TaggedItemsRepositoryWithBagRelationships {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<TaggedItems> fetchBagRelationships(Optional<TaggedItems> taggedItems) {
        return taggedItems.map(this::fetchPages).map(this::fetchPosts);
    }

    @Override
    public Page<TaggedItems> fetchBagRelationships(Page<TaggedItems> taggedItems) {
        return new PageImpl<>(fetchBagRelationships(taggedItems.getContent()), taggedItems.getPageable(), taggedItems.getTotalElements());
    }

    @Override
    public List<TaggedItems> fetchBagRelationships(List<TaggedItems> taggedItems) {
        return Optional.of(taggedItems).map(this::fetchPages).map(this::fetchPosts).orElse(Collections.emptyList());
    }

    TaggedItems fetchPages(TaggedItems result) {
        return entityManager
            .createQuery(
                "select taggedItems from TaggedItems taggedItems left join fetch taggedItems.pages where taggedItems.id = :id",
                TaggedItems.class
            )
            .setParameter("id", result.getId())
            .getSingleResult();
    }

    List<TaggedItems> fetchPages(List<TaggedItems> taggedItems) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, taggedItems.size()).forEach(index -> order.put(taggedItems.get(index).getId(), index));
        List<TaggedItems> result = entityManager
            .createQuery(
                "select taggedItems from TaggedItems taggedItems left join fetch taggedItems.pages where taggedItems in :taggedItems",
                TaggedItems.class
            )
            .setParameter("taggedItems", taggedItems)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }

    TaggedItems fetchPosts(TaggedItems result) {
        return entityManager
            .createQuery(
                "select taggedItems from TaggedItems taggedItems left join fetch taggedItems.posts where taggedItems.id = :id",
                TaggedItems.class
            )
            .setParameter("id", result.getId())
            .getSingleResult();
    }

    List<TaggedItems> fetchPosts(List<TaggedItems> taggedItems) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, taggedItems.size()).forEach(index -> order.put(taggedItems.get(index).getId(), index));
        List<TaggedItems> result = entityManager
            .createQuery(
                "select taggedItems from TaggedItems taggedItems left join fetch taggedItems.posts where taggedItems in :taggedItems",
                TaggedItems.class
            )
            .setParameter("taggedItems", taggedItems)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
