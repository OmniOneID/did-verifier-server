package org.omnione.did.verifier.v1.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.omnione.did.base.db.domain.PolicyProfile;
import org.omnione.did.base.db.domain.VpFilter;
import org.omnione.did.base.db.repository.PolicyProfileRepository;
import org.omnione.did.base.db.repository.VpFilterRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.util.BaseMultibaseUtil;
import org.omnione.did.common.util.JsonUtil;
import org.omnione.did.verifier.v1.admin.dto.CombinedIdListDto;
import org.omnione.did.verifier.v1.admin.dto.FilterDTO;
import org.omnione.did.verifier.v1.admin.api.ListFeign;
import org.omnione.did.verifier.v1.admin.dto.VcSchemaListResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FilterService {
    private final VpFilterRepository vpFilterRepository;
    private final FilterQueryService filterQueryService;
    private final PolicyProfileRepository policyProfileRepository;
    private final ListFeign listFeign;
    private final ObjectMapper objectMapper;


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


        return FilterDTO.fromVpFilter(vpFilterRepository.save(existingFilter));
    }

    public FilterDTO getFilterInfo(long filterId) {
        VpFilter vpFilter = vpFilterRepository.findById(filterId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_FILTER_NOT_FOUND));

        return FilterDTO.fromVpFilter(vpFilter);
    }

    public Page<FilterDTO> searchFilterList(String searchKey, String searchValue, Pageable pageable) {
        return filterQueryService.searchFilterList(searchKey, searchValue, pageable);
    }

    @Transactional
    public void deleteFilter(long filterId) {
        VpFilter vpFilter = vpFilterRepository.findById(filterId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_FILTER_NOT_FOUND));
        Optional<PolicyProfile> referencingProfiles = policyProfileRepository.findByFilterId(vpFilter.getFilterId());
        if (referencingProfiles.isPresent()) {
            throw new OpenDidException(ErrorCode.VP_FILTER_IN_USE);
        }

        vpFilterRepository.delete(vpFilter);
    }


    public List<FilterDTO> getFilterList(String title) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));

        List<VpFilter> vpFilterList;
        if(Objects.equals(title, "all")){
            vpFilterList = vpFilterRepository.findAll(sort);
        } else if (title != null && !title.isEmpty()) {
            vpFilterList = vpFilterRepository.findByTitleContainingIgnoreCase(title, sort);
        } else {
            vpFilterList = vpFilterRepository.findAll(sort);
        }


        return vpFilterList.stream()
                .map(FilterDTO::fromVpFilter)
                .collect(Collectors.toList());
    }

    public VcSchemaListResDto getVcSchemas() {
        try {
            String jsonString = listFeign.requestVcSchemaList();

            return objectMapper.readValue(jsonString, VcSchemaListResDto.class);

        } catch (JsonProcessingException e) {
            throw new OpenDidException(ErrorCode.VC_SCHEMA_NOT_FOUND);
        }
    }
}
