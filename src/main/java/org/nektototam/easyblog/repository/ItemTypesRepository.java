package org.nektototam.easyblog.repository;

import org.nektototam.easyblog.domain.ItemTypes;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ItemTypes entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ItemTypesRepository extends JpaRepository<ItemTypes, Long> {}
