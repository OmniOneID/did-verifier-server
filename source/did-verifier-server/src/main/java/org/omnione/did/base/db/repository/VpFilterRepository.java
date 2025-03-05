package org.omnione.did.base.db.repository;

import org.omnione.did.base.db.domain.VpFilter;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * The VpFilterRepository interface provides methods for querying the database for VP filters.
 * It is designed to facilitate the retrieval of VP filters from the database, ensuring that the data is accurate and up-to-date.
 */
public interface VpFilterRepository extends JpaRepository<VpFilter, Long> {
    Optional<VpFilter> findByFilterId(Long filterId);

    List<VpFilter> findByTitle(String title, Sort sort);
}
