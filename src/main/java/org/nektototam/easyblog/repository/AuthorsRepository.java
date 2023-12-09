package org.nektototam.easyblog.repository;

import org.nektototam.easyblog.domain.Authors;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Authors entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuthorsRepository extends JpaRepository<Authors, Long> {}
