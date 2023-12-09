package org.nektototam.easyblog.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import java.util.stream.Stream;
import org.nektototam.easyblog.domain.Comments;
import org.nektototam.easyblog.repository.CommentsRepository;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Comments} entity.
 */
public interface CommentsSearchRepository extends ElasticsearchRepository<Comments, Long>, CommentsSearchRepositoryInternal {}

interface CommentsSearchRepositoryInternal {
    Stream<Comments> search(String query);

    Stream<Comments> search(Query query);

    @Async
    void index(Comments entity);

    @Async
    void deleteFromIndexById(Long id);
}

class CommentsSearchRepositoryInternalImpl implements CommentsSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final CommentsRepository repository;

    CommentsSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, CommentsRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Comments> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Comments> search(Query query) {
        return elasticsearchTemplate.search(query, Comments.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Comments entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Comments.class);
    }
}
