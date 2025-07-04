package org.omnione.did.base.db.repository;

import org.omnione.did.base.db.domain.PolicyProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PolicyProfileRepositoryAdmin {
    Page<PolicyProfile> searchPolicyProfileList(String searchKey, String searchValue, Pageable pageable);

}
