package org.omnione.did.verifier.v1.admin.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.domain.VpFilter;
import org.omnione.did.base.db.repository.VpFilterRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.verifier.v1.admin.dto.FilterDTO;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FilterServiceImpl implements FilterService {
    private final VpFilterRepository vpFilterRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<FilterDTO> getFilterList(String title) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));

        List<VpFilter> vpFilterList;
        if (title != null && !title.isEmpty()) {
            vpFilterList = vpFilterRepository.findByTitle(title, sort);
        } else {
            vpFilterList = vpFilterRepository.findAll(sort);
        }


        return vpFilterList.stream()
                .map(filter -> modelMapper.map(filter, FilterDTO.class))
                .collect(Collectors.toList());
    }

    @Override
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
                .value(filterDTO.getValue())
                .present_all(filterDTO.isPresentAll())
                .build();

        vpFilterRepository.save(vpFilter);
    }

    @Override
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

        // 업데이트 후 저장
        return modelMapper.map(vpFilterRepository.save(existingFilter), FilterDTO.class);
    }

    @Override
    public FilterDTO getFilterInfo(long filterId) {
        VpFilter vpFilter = vpFilterRepository.findById(filterId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_FILTER_NOT_FOUND));

        // 필터 정보를 DTO로 반환
        return modelMapper.map(vpFilter, FilterDTO.class);
    }

    @Override
    @Transactional
    public void addFilter(String filterName, String filterValue) {
        // 필터 저장
        vpFilterRepository.save(VpFilter.builder()
                .title(filterName)
                .value(filterValue)
                .build());
    }

    @Override
    @Transactional
    public void deleteFilter(long filterId) {
        VpFilter vpFilter = vpFilterRepository.findById(filterId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_FILTER_NOT_FOUND));

        // 필터 삭제
        vpFilterRepository.delete(vpFilter);
    }
}
