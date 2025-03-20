package org.omnione.did.verifier.v1.admin.service;

import lombok.RequiredArgsConstructor;
import org.omnione.did.base.db.domain.VpFilter;
import org.omnione.did.base.db.repository.VpFilterRepository;
import org.omnione.did.verifier.v1.admin.dto.FilterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FilterQueryService {

    private final VpFilterRepository filterRepository;

    public Page<FilterDTO> searchFilterList(String searchKey, String searchValue, Pageable pageable) {
        Page<VpFilter> vpFilters = filterRepository.searchVpFilterList(searchKey, searchValue, pageable);

        List<FilterDTO> filterDtos = vpFilters.getContent().stream()
                .map(FilterDTO::fromVpFilter)
                .collect(Collectors.toList());

        return new PageImpl<>(filterDtos, pageable, vpFilters.getTotalElements());

    }}
