package org.omnione.did.verifier.v1.admin.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.domain.Payload;
import org.omnione.did.base.db.repository.PayloadRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.verifier.v1.admin.dto.PayloadDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * The AdminServiceImpl class provides methods for saving and getting verifier information.
 */
@RequiredArgsConstructor
@Service
public class PayloadService {
    private final PayloadRepository payloadRepository;
    private final ModelMapper modelMapper;
    private final PayloadQueryService payloadQueryService;

    @Transactional
    public void savePayload(PayloadDTO payloadDTO) {

        Payload payload = Payload.builder()
                .payloadId(UUID.randomUUID().toString())
                .service(payloadDTO.getService())
                .device(payloadDTO.getDevice())
                .locked(payloadDTO.isLocked())
                .mode(payloadDTO.getMode())
                .endpoints(payloadDTO.getEndpoints())
                .validSecond(payloadDTO.getValidSecond())
                .build();

        payloadRepository.save(payload);
    }

    @Transactional
    public PayloadDTO updatePayload(PayloadDTO reqPayloadDto) {

        Payload existingPayload = payloadRepository.findById(reqPayloadDto.getId())
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PAYLOAD_NOT_FOUND));

        existingPayload.setService(reqPayloadDto.getService());
        existingPayload.setDevice(reqPayloadDto.getDevice());
        existingPayload.setLocked(reqPayloadDto.isLocked());
        existingPayload.setMode(reqPayloadDto.getMode());
        existingPayload.setEndpoints(reqPayloadDto.getEndpoints());
        existingPayload.setValidSecond(reqPayloadDto.getValidSecond());

        return modelMapper.map(payloadRepository.save(existingPayload), PayloadDTO.class);
    }

    public PayloadDTO getPayloadInfo(Long id) {
        Payload payload = payloadRepository.findById(id)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PAYLOAD_NOT_FOUND));
        return modelMapper.map(payload, PayloadDTO.class);
    }

    @Transactional
    public void deletePayload(long id) {
        Payload payload = payloadRepository.findById(id)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PAYLOAD_NOT_FOUND));

        payloadRepository.delete(payload);
    }

    public Page<PayloadDTO> searchPayloadList(String searchKey, String searchValue, Pageable pageable) {
        return payloadQueryService.searchPayloadList(searchKey, searchValue, pageable);
    }
}
