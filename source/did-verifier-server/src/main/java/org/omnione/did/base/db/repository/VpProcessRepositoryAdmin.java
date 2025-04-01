package org.omnione.did.base.db.repository;


import org.omnione.did.base.db.domain.VpProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VpProcessRepositoryAdmin {
    Page<VpProcess> searchVpProcessList(String searchKey, String searchValue, Pageable pageable);
}
