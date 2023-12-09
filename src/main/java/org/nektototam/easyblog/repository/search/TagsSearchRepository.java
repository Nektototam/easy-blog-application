package org.nektototam.easyblog.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import java.util.stream.Stream;
import org.nektototam.easyblog.domain.Tags;
import org.nektototam.easyblog.repository.TagsRepository;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Tags} entity.
 */
public interface TagsSearchRepository extends ElasticsearchRepository<Tags, Long>, TagsSearchRepositoryInternal {}

interface TagsSearchRepositoryInternal {
    Stream<Tags> search(String query);

    Stream<Tags> search(Query query);

    @Async
    void index(Tags entity);

    @Async
    void deleteFromIndexById(Long id);
}

class TagsSearchRepositoryInternalImpl implements TagsSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final TagsRepository repository;

    TagsSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, TagsRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Tags> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Tags> search(Query query) {
        return elasticsearchTemplate.search(query, Tags.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Tags entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Tags.class);
    }
}
