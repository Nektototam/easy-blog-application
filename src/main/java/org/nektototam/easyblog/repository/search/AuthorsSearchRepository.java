package org.nektototam.easyblog.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import java.util.stream.Stream;
import org.nektototam.easyblog.domain.Authors;
import org.nektototam.easyblog.repository.AuthorsRepository;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Authors} entity.
 */
public interface AuthorsSearchRepository extends ElasticsearchRepository<Authors, Long>, AuthorsSearchRepositoryInternal {}

interface AuthorsSearchRepositoryInternal {
    Stream<Authors> search(String query);

    Stream<Authors> search(Query query);

    @Async
    void index(Authors entity);

    @Async
    void deleteFromIndexById(Long id);
}

class AuthorsSearchRepositoryInternalImpl implements AuthorsSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final AuthorsRepository repository;

    AuthorsSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, AuthorsRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Authors> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Authors> search(Query query) {
        return elasticsearchTemplate.search(query, Authors.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Authors entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Authors.class);
    }
}
