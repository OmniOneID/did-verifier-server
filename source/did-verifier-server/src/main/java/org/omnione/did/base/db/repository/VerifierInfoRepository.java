package org.omnione.did.base.db.repository;

import org.omnione.did.base.db.domain.VerifierInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerifierInfoRepository extends JpaRepository<VerifierInfo, Long> {
    Optional<VerifierInfo> findFirstBy();
}
