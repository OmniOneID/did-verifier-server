package org.omnione.did.verifier.v1.admin.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.domain.Payload;
import org.omnione.did.base.db.repository.PayloadRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.property.VerifierProperty;
import org.omnione.did.verifier.v1.admin.dto.PayloadDTO;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The AdminServiceImpl class provides methods for saving and getting verifier information.
 */
@RequiredArgsConstructor
@Service
public class PayloadServiceImpl implements PayloadService {
    private final PayloadRepository payloadRepository;
    private final ModelMapper modelMapper;

    private VerifierProperty verifierProperty;

    @Override
    public String getVerifierInfo() {

        return "";
    }

    @Override
    public List<PayloadDTO> getPayloadList(String service) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));

        // service 값이 존재하면 해당 값으로 필터링, 없으면 모든 데이터를 조회
        List<Payload> payloadList;
        if (service != null && !service.isEmpty()) {
            payloadList = payloadRepository.findByService(service, sort);
        } else {
            payloadList = payloadRepository.findAll(sort);
        }

        // Payload 객체 리스트를 PayloadDTO 객체 리스트로 변환
        return payloadList.stream()
                .map(payload -> modelMapper.map(payload, PayloadDTO.class))
                .collect(Collectors.toList());
    }


    @Override
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

    @Override
    @Transactional
    public Payload updatePayload(PayloadDTO reqPayloadDto) {

        Payload existingPayload = payloadRepository.findByPayloadId(reqPayloadDto.getPayloadId())
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PAYLOAD_NOT_FOUND));

        existingPayload.setService(reqPayloadDto.getService());
        existingPayload.setDevice(reqPayloadDto.getDevice());
        existingPayload.setLocked(reqPayloadDto.isLocked());
        existingPayload.setMode(reqPayloadDto.getMode());
        existingPayload.setEndpoints(reqPayloadDto.getEndpoints());
        existingPayload.setValidSecond(reqPayloadDto.getValidSecond());

        return payloadRepository.save(existingPayload);
    }

    @Override
    public PayloadDTO getPayloadInfo(long id) {
        Payload payload = payloadRepository.findById(id)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PAYLOAD_NOT_FOUND));
        return modelMapper.map(payload, PayloadDTO.class);
    }

    @Override
    @Transactional
    public void deletePayload(long id) {
        Payload payload = payloadRepository.findById(id)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PAYLOAD_NOT_FOUND));

        payloadRepository.delete(payload);
    }



}
