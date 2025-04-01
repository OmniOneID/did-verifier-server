package org.omnione.did.verifier.v1.admin.service;

import lombok.RequiredArgsConstructor;
import org.omnione.did.verifier.v1.admin.dto.VpSubmitDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class VpSubmitService {
    private final VpSubmitQueryService payloadQueryService;
    
    public Page<VpSubmitDTO> searchVpSubmitList(String searchKey, String searchValue, Pageable pageable) {
        return payloadQueryService.searchVpSubmitList(searchKey, searchValue, pageable);
    }
}
