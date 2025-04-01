package org.omnione.did.verifier.v1.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.constants.UrlConstant;
import org.omnione.did.base.db.domain.VpSubmit;
import org.omnione.did.base.db.repository.VpSubmitRepository;
import org.omnione.did.verifier.v1.admin.dto.VpSubmitDTO;
import org.omnione.did.verifier.v1.admin.service.VpSubmitService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = UrlConstant.Verifier.ADMIN)
public class VpSubmitController {
    private final VpSubmitService vpSubmitService;

    @Operation(summary = "Get VP Submit List", description = "Get a VP Submit List.")
    @GetMapping(UrlConstant.Verifier.GET_VP_SUBMIT_LIST)
    public Page<VpSubmitDTO> searchVpSubmitList(String searchKey, String searchValue, Pageable pageable) {
        return vpSubmitService.searchVpSubmitList(searchKey, searchValue, pageable);
    }

}
