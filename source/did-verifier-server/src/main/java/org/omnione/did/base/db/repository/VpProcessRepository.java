package org.omnione.did.base.db.repository;

import org.omnione.did.base.db.domain.VpProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

/**
 * The VpProcessRepository interface provides methods for querying the database for VP processes.
 * It is designed to facilitate the retrieval of VP processes from the database, ensuring that the data is accurate and up-to-date.
 */
public interface VpProcessRepository extends JpaRepository<VpProcess, Long>, QuerydslPredicateExecutor<VpProcess>, VpProcessRepositoryAdmin {
    List<VpProcess> findByTitleContainingIgnoreCase(String title);


}
