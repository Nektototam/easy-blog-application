package org.nektototam.easyblog.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.nektototam.easyblog.domain.Pages;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class PagesRepositoryWithBagRelationshipsImpl implements PagesRepositoryWithBagRelationships {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Pages> fetchBagRelationships(Optional<Pages> pages) {
        return pages.map(this::fetchAuthors);
    }

    @Override
    public Page<Pages> fetchBagRelationships(Page<Pages> pages) {
        return new PageImpl<>(fetchBagRelationships(pages.getContent()), pages.getPageable(), pages.getTotalElements());
    }

    @Override
    public List<Pages> fetchBagRelationships(List<Pages> pages) {
        return Optional.of(pages).map(this::fetchAuthors).orElse(Collections.emptyList());
    }

    Pages fetchAuthors(Pages result) {
        return entityManager
            .createQuery("select pages from Pages pages left join fetch pages.authors where pages.id = :id", Pages.class)
            .setParameter("id", result.getId())
            .getSingleResult();
    }

    List<Pages> fetchAuthors(List<Pages> pages) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, pages.size()).forEach(index -> order.put(pages.get(index).getId(), index));
        List<Pages> result = entityManager
            .createQuery("select pages from Pages pages left join fetch pages.authors where pages in :pages", Pages.class)
            .setParameter("pages", pages)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
