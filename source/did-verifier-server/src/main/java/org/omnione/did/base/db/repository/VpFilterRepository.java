package org.omnione.did.base.db.repository;

import org.omnione.did.base.db.domain.VpFilter;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

/**
 * The VpFilterRepository interface provides methods for querying the database for VP filters.
 * It is designed to facilitate the retrieval of VP filters from the database, ensuring that the data is accurate and up-to-date.
 */
public interface VpFilterRepository extends JpaRepository<VpFilter, Long>, QuerydslPredicateExecutor<VpFilter>, VpFilterRepositoryAdmin {
    Optional<VpFilter> findByFilterId(Long filterId);

    List<VpFilter> findByTitleContainingIgnoreCase(String title, Sort sort);
}
