package org.omnione.did.verifier.v1.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.constants.UrlConstant;
import org.omnione.did.verifier.v1.admin.dto.FilterDTO;
import org.omnione.did.verifier.v1.admin.service.FilterService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * The FilterController class provides methods for managing filters in the DID Verifier application.
 * It is designed to facilitate the retrieval, creation, updating, and deletion of filters in the application.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = UrlConstant.Verifier.ADMIN)
public class FilterController {

    private final FilterService filterService;

    @Operation(summary = "Get Filter List", description = "Get a list of filters by title (optional).")
    @GetMapping(UrlConstant.Verifier.GET_FILTER_LIST)
    public Page<FilterDTO> searchFilterList(String searchKey, String searchValue, Pageable pageable) {
        return filterService.searchFilterList(searchKey, searchValue, pageable);
    }

    @Operation(summary = "Get Filter Info", description = "Get a single filter's information.")
    @GetMapping(UrlConstant.Verifier.GET_FILTER_INFO)
    public FilterDTO getFilterInfo(@PathVariable Long filterId) {
        return filterService.getFilterInfo(filterId);
    }

    @Operation(summary = "Save Filter", description = "Save a new filter.")
    @PostMapping(UrlConstant.Verifier.SAVE_FILTER_INFO)
    public ResponseEntity<Void> saveFilter(@RequestBody FilterDTO filterDTO) {
        filterService.saveFilter(filterDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update Filter", description = "Update an existing filter.")
    @PutMapping(UrlConstant.Verifier.UPDATE_FILTER_INFO)
    public ResponseEntity<FilterDTO> updateFilter(@RequestBody FilterDTO filterDTO) {
        FilterDTO updatedFilterDTO = filterService.updateFilter(filterDTO);
        return ResponseEntity.ok(updatedFilterDTO);
    }

    @Operation(summary = "Delete Filter", description = "Delete a filter by ID.")
    @DeleteMapping(UrlConstant.Verifier.DELETE_FILTER_INFO)
    public ResponseEntity<Void> deleteFilter(@PathVariable Long filterId) {
        filterService.deleteFilter(filterId);
        return ResponseEntity.noContent().build();
    }
}
