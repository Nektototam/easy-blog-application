package org.nektototam.easyblog.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import java.util.stream.Stream;
import org.nektototam.easyblog.domain.TaggedItems;
import org.nektototam.easyblog.repository.TaggedItemsRepository;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link TaggedItems} entity.
 */
public interface TaggedItemsSearchRepository extends ElasticsearchRepository<TaggedItems, Long>, TaggedItemsSearchRepositoryInternal {}

interface TaggedItemsSearchRepositoryInternal {
    Stream<TaggedItems> search(String query);

    Stream<TaggedItems> search(Query query);

    @Async
    void index(TaggedItems entity);

    @Async
    void deleteFromIndexById(Long id);
}

class TaggedItemsSearchRepositoryInternalImpl implements TaggedItemsSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final TaggedItemsRepository repository;

    TaggedItemsSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, TaggedItemsRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<TaggedItems> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<TaggedItems> search(Query query) {
        return elasticsearchTemplate.search(query, TaggedItems.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(TaggedItems entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), TaggedItems.class);
    }
}
