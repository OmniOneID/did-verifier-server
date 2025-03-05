package org.omnione.did.verifier.v1.admin.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.domain.Payload;
import org.omnione.did.base.db.domain.VpProcess;
import org.omnione.did.base.db.repository.VpProcessRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.verifier.v1.admin.dto.PayloadDTO;
import org.omnione.did.verifier.v1.admin.dto.ProcessDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.omnione.did.base.exception.ErrorCode.VP_PROCESS_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class ProcessServiceImpl implements ProcessService {

    private final VpProcessRepository vpProcessRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<ProcessDTO> getProcessList(String title) {
        List<VpProcess> processList;

        if (title != null && !title.isEmpty()) {
            processList = vpProcessRepository.findByTitle(title);
        } else {
            processList = vpProcessRepository.findAll();
        }

        return processList.stream()
                .map(ProcessDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ProcessDTO getProcessInfo(long processId) {
        VpProcess process = vpProcessRepository.findById(processId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PROCESS_NOT_FOUND));
        return ProcessDTO.fromEntity(process);
    }

    @Override
    @Transactional
    public void saveProcess(ProcessDTO processDTO) {
        VpProcess process = processDTO.toEntity();
        vpProcessRepository.save(process);
    }

    @Override
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

    @Override
    @Transactional
    public void deleteProcess(long processId) {
        VpProcess process = vpProcessRepository.findById(processId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PROCESS_NOT_FOUND));
        vpProcessRepository.delete(process);
    }
}
