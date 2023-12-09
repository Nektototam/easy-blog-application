package org.nektototam.easyblog.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import java.util.stream.Stream;
import org.nektototam.easyblog.domain.Pages;
import org.nektototam.easyblog.repository.PagesRepository;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Pages} entity.
 */
public interface PagesSearchRepository extends ElasticsearchRepository<Pages, Long>, PagesSearchRepositoryInternal {}

interface PagesSearchRepositoryInternal {
    Stream<Pages> search(String query);

    Stream<Pages> search(Query query);

    @Async
    void index(Pages entity);

    @Async
    void deleteFromIndexById(Long id);
}

class PagesSearchRepositoryInternalImpl implements PagesSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final PagesRepository repository;

    PagesSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, PagesRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Pages> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Pages> search(Query query) {
        return elasticsearchTemplate.search(query, Pages.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Pages entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Pages.class);
    }
}
