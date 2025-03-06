package org.omnione.did.base.db.repository;

import org.omnione.did.base.db.domain.Payload;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payload entity operations.
 * Provides CRUD operations for Payload entities and custom query methods.
 *
 */
public interface PayloadRepository extends JpaRepository<Payload, Long>, QuerydslPredicateExecutor<Payload>, PayloadRepositoryAdmin {

    Optional<Payload> findByPayloadId(String payloadId);

    List<Payload> findByService(String service, Sort sort);
}
