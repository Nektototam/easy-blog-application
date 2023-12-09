package org.nektototam.easyblog.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import java.util.stream.Stream;
import org.nektototam.easyblog.domain.Users;
import org.nektototam.easyblog.repository.UsersRepository;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Users} entity.
 */
public interface UsersSearchRepository extends ElasticsearchRepository<Users, Long>, UsersSearchRepositoryInternal {}

interface UsersSearchRepositoryInternal {
    Stream<Users> search(String query);

    Stream<Users> search(Query query);

    @Async
    void index(Users entity);

    @Async
    void deleteFromIndexById(Long id);
}

class UsersSearchRepositoryInternalImpl implements UsersSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final UsersRepository repository;

    UsersSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, UsersRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Users> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Users> search(Query query) {
        return elasticsearchTemplate.search(query, Users.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Users entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Users.class);
    }
}
