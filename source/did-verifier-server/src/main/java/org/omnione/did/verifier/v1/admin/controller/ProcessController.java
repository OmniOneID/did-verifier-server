package org.omnione.did.verifier.v1.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.constants.UrlConstant;
import org.omnione.did.verifier.v1.admin.dto.ProcessDTO;
import org.omnione.did.verifier.v1.admin.service.ProcessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = UrlConstant.Verifier.ADMIN)
public class ProcessController {

    private final ProcessService processService;

    @Operation(summary = "Get Process List", description = "Get a list of processes by name (optional).")
    @GetMapping(UrlConstant.Verifier.GET_PROCESS_LIST)
    public Page<ProcessDTO> searchVpProcessList(String searchKey, String searchValue, Pageable pageable) {
        return processService.searchVpProcessList(searchKey, searchValue, pageable);
    }

    @Operation(summary = "Get Process Info", description = "Get a single process's information.")
    @GetMapping(UrlConstant.Verifier.GET_PROCESS_INFO)
    public ProcessDTO getProcessInfo(@PathVariable Long id) {
        return processService.getProcessInfo(id);
    }

    @Operation(summary = "Save Process", description = "Save a new process.")
    @PostMapping(UrlConstant.Verifier.SAVE_PROCESS_INFO)
    public ResponseEntity<Void> saveProcess(@RequestBody ProcessDTO processDTO) {
        processService.saveProcess(processDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update Process", description = "Update an existing process.")
    @PutMapping(UrlConstant.Verifier.UPDATE_PROCESS_INFO)
    public ResponseEntity<ProcessDTO> updateProcess(@RequestBody ProcessDTO processDTO) {
        ProcessDTO updatedProcessDTO = processService.updateProcess(processDTO);
        return ResponseEntity.ok(updatedProcessDTO);
    }

    @Operation(summary = "Delete Process", description = "Delete a process by ID.")
    @DeleteMapping(UrlConstant.Verifier.DELETE_PROCESS_INFO)
    public ResponseEntity<Void> deleteProcess(@PathVariable Long id) {
        processService.deleteProcess(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search Process List", description = "Get a list of filters by title (optional).")
    @GetMapping(UrlConstant.Verifier.GET_POPUP_PROCESS_LIST)
    public List<ProcessDTO> getProcessList(@PathVariable String searchValue) {
        return processService.getProcessList(searchValue);
    }
}
