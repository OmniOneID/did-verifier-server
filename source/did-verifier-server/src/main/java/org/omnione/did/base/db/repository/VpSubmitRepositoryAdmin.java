package org.omnione.did.base.db.repository;

import org.omnione.did.base.db.domain.VpSubmit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VpSubmitRepositoryAdmin {
    Page<VpSubmit> searchVpSubmitList(String searchKey, String searchValue, Pageable pageable);

}
