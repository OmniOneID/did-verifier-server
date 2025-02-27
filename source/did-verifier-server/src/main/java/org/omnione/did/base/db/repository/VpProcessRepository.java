package org.omnione.did.base.db.repository;

import org.jetbrains.annotations.NotNull;
import org.omnione.did.base.db.domain.VpProcess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * The VpProcessRepository interface provides methods for querying the database for VP processes.
 * It is designed to facilitate the retrieval of VP processes from the database, ensuring that the data is accurate and up-to-date.
 */
public interface VpProcessRepository extends JpaRepository<VpProcess, Long> {
    Optional<VpProcess> findById(Long id);
}
