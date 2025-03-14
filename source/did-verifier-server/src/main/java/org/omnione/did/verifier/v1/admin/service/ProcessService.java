package org.omnione.did.verifier.v1.admin.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.domain.VpFilter;
import org.omnione.did.base.db.domain.VpProcess;
import org.omnione.did.base.db.repository.VpProcessRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.verifier.v1.admin.dto.FilterDTO;
import org.omnione.did.verifier.v1.admin.dto.ProcessDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProcessService {
    private final VpProcessRepository vpProcessRepository;
    private final ModelMapper modelMapper;
    private final ProcessQueryService processQueryService;

    public ProcessDTO getProcessInfo(long processId) {
        VpProcess process = vpProcessRepository.findById(processId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PROCESS_NOT_FOUND));
        return ProcessDTO.fromEntity(process);
    }


    @Transactional
    public void saveProcess(ProcessDTO processDTO) {
        VpProcess process = processDTO.toEntity();
        vpProcessRepository.save(process);
    }

    @Transactional
    public ProcessDTO updateProcess(ProcessDTO processDTO) {
        VpProcess existingProcess = vpProcessRepository.findById(processDTO.getId())
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PROCESS_NOT_FOUND));
        existingProcess.setTitle(processDTO.getTitle());
        existingProcess.setAuthType(processDTO.getAuthType());
        existingProcess.setCurve(processDTO.getReqE2e().getCurve());
        existingProcess.setCipher(processDTO.getReqE2e().getCipher());
        existingProcess.setPadding(processDTO.getReqE2e().getPadding());
        existingProcess.setEndpoints(processDTO.getEndpoints());

        return modelMapper.map(vpProcessRepository.save(existingProcess), ProcessDTO.class);
    }

    public Page<ProcessDTO> searchVpProcessList(String searchKey, String searchValue, Pageable pageable) {
        return processQueryService.searchVpProcessList(searchKey, searchValue, pageable);
    }

    @Transactional
    public void deleteProcess(long processId) {
        VpProcess process = vpProcessRepository.findById(processId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PROCESS_NOT_FOUND));
        vpProcessRepository.delete(process);
    }


    public List<ProcessDTO> getProcessList(String searchValue) {

        Sort sort = Sort.by(Sort.Order.desc("createdAt"));

        List<VpProcess> vpProcessList;
        if(Objects.equals(searchValue, "all")){
            vpProcessList = vpProcessRepository.findAll(sort);
        } else if (searchValue != null && !searchValue.isEmpty()) {
            vpProcessList = vpProcessRepository.findByTitle(searchValue);
        } else {
            vpProcessList = vpProcessRepository.findAll(sort);
        }


        return vpProcessList.stream()
                .map(process -> modelMapper.map(process, ProcessDTO.class))
                .collect(Collectors.toList());
    }
}
