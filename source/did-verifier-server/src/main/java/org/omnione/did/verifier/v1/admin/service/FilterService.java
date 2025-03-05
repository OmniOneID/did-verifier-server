package org.omnione.did.verifier.v1.admin.service;


import org.omnione.did.verifier.v1.admin.dto.FilterDTO;

import java.util.List;
import java.util.Optional;

public interface FilterService {
    void addFilter(String filterName, String filterValue);
    
    void deleteFilter(long filterId);

    FilterDTO updateFilter(FilterDTO filterDTO);

    void saveFilter(FilterDTO filterDTO);

    List<FilterDTO> getFilterList(String title);

    FilterDTO getFilterInfo(long filterId);

}
