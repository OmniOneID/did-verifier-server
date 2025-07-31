package org.omnione.did.verifier.v1.admin.service;

import lombok.RequiredArgsConstructor;
import org.omnione.did.base.db.domain.VpProcess;
import org.omnione.did.base.db.repository.VpProcessRepository;
import org.omnione.did.verifier.v1.admin.dto.ProcessDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProcessQueryService {
    private final VpProcessRepository vpProcessRepository;

    public Page<ProcessDTO> searchVpProcessList(String searchKey, String searchValue, Pageable pageable) {
        Page<VpProcess> processPage = vpProcessRepository.searchVpProcessList(searchKey, searchValue, pageable);

        List<ProcessDTO> processDtos = processPage.getContent().stream()
                .map(ProcessDTO::fromEntity)
                .toList();

        return new PageImpl<>(processDtos, pageable, processPage.getTotalElements());
    }
}
