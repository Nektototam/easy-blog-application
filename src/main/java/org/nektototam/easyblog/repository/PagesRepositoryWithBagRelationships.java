package org.nektototam.easyblog.repository;

import java.util.List;
import java.util.Optional;
import org.nektototam.easyblog.domain.Pages;
import org.springframework.data.domain.Page;

public interface PagesRepositoryWithBagRelationships {
    Optional<Pages> fetchBagRelationships(Optional<Pages> pages);

    List<Pages> fetchBagRelationships(List<Pages> pages);

    Page<Pages> fetchBagRelationships(Page<Pages> pages);
}
