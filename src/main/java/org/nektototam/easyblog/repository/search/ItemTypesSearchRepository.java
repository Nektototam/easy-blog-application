package org.nektototam.easyblog.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import java.util.stream.Stream;
import org.nektototam.easyblog.domain.ItemTypes;
import org.nektototam.easyblog.repository.ItemTypesRepository;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link ItemTypes} entity.
 */
public interface ItemTypesSearchRepository extends ElasticsearchRepository<ItemTypes, Long>, ItemTypesSearchRepositoryInternal {}

interface ItemTypesSearchRepositoryInternal {
    Stream<ItemTypes> search(String query);

    Stream<ItemTypes> search(Query query);

    @Async
    void index(ItemTypes entity);

    @Async
    void deleteFromIndexById(Long id);
}

class ItemTypesSearchRepositoryInternalImpl implements ItemTypesSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final ItemTypesRepository repository;

    ItemTypesSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, ItemTypesRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<ItemTypes> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<ItemTypes> search(Query query) {
        return elasticsearchTemplate.search(query, ItemTypes.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(ItemTypes entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), ItemTypes.class);
    }
}
