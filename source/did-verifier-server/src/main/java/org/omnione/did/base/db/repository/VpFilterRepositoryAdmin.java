package org.omnione.did.base.db.repository;

import org.omnione.did.base.db.domain.VpFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VpFilterRepositoryAdmin {
    Page<VpFilter> searchVpFilterList(String searchKey, String searchValue, Pageable pageable);
}
