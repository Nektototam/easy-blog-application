package org.nektototam.easyblog.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import java.util.stream.Stream;
import org.nektototam.easyblog.domain.Posts;
import org.nektototam.easyblog.repository.PostsRepository;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Posts} entity.
 */
public interface PostsSearchRepository extends ElasticsearchRepository<Posts, Long>, PostsSearchRepositoryInternal {}

interface PostsSearchRepositoryInternal {
    Stream<Posts> search(String query);

    Stream<Posts> search(Query query);

    @Async
    void index(Posts entity);

    @Async
    void deleteFromIndexById(Long id);
}

class PostsSearchRepositoryInternalImpl implements PostsSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final PostsRepository repository;

    PostsSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, PostsRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Posts> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Posts> search(Query query) {
        return elasticsearchTemplate.search(query, Posts.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Posts entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Posts.class);
    }
}
