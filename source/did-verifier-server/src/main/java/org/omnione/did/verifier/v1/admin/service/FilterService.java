package org.omnione.did.verifier.v1.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.domain.VpFilter;
import org.omnione.did.base.db.repository.VpFilterRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.util.BaseMultibaseUtil;
import org.omnione.did.common.util.JsonUtil;
import org.omnione.did.verifier.v1.admin.dto.FilterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FilterService {
    private final VpFilterRepository vpFilterRepository;
    private final ModelMapper modelMapper;
    private final FilterQueryService filterQueryService;

    @Transactional
    public void saveFilter(FilterDTO filterDTO) {
        VpFilter vpFilter = VpFilter.builder()
                .filterId(UUID.randomUUID().getMostSignificantBits())
                .title(filterDTO.getTitle())
                .id(filterDTO.getId())
                .type(filterDTO.getType())
                .requiredClaims(filterDTO.getRequiredClaims())
                .allowedIssuers(filterDTO.getAllowedIssuers())
                .displayClaims(filterDTO.getDisplayClaims())
                .present_all(filterDTO.isPresentAll())
                .build();
        try {
            String serializeToFilter = JsonUtil.serializeToJson(filterDTO);
            String value = BaseMultibaseUtil.encode(serializeToFilter.getBytes(StandardCharsets.UTF_8));
            vpFilter.setValue(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        vpFilterRepository.save(vpFilter);
    }

    @Transactional
    public FilterDTO updateFilter(FilterDTO reqFilterDto) {

        VpFilter existingFilter = vpFilterRepository.findById(reqFilterDto.getFilterId())
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_FILTER_NOT_FOUND));

        existingFilter.setTitle(reqFilterDto.getTitle());
        existingFilter.setId(reqFilterDto.getId());
        existingFilter.setType(reqFilterDto.getType());
        existingFilter.setRequiredClaims(reqFilterDto.getRequiredClaims());
        existingFilter.setAllowedIssuers(reqFilterDto.getAllowedIssuers());
        existingFilter.setDisplayClaims(reqFilterDto.getDisplayClaims());
        existingFilter.setValue(reqFilterDto.getValue());
        existingFilter.setPresent_all(reqFilterDto.isPresentAll());


        return modelMapper.map(vpFilterRepository.save(existingFilter), FilterDTO.class);
    }

    public FilterDTO getFilterInfo(long filterId) {
        VpFilter vpFilter = vpFilterRepository.findById(filterId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_FILTER_NOT_FOUND));

        return modelMapper.map(vpFilter, FilterDTO.class);
    }

    public Page<FilterDTO> searchFilterList(String searchKey, String searchValue, Pageable pageable) {
        return filterQueryService.searchFilterList(searchKey, searchValue, pageable);
    }

    @Transactional
    public void deleteFilter(long filterId) {
        VpFilter vpFilter = vpFilterRepository.findById(filterId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_FILTER_NOT_FOUND));

        vpFilterRepository.delete(vpFilter);
    }
}
