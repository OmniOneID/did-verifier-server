package org.omnione.did.verifier.v1.admin.service;

import org.omnione.did.verifier.v1.admin.dto.PayloadDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface PayloadService {


    String getVerifierInfo();

    List<PayloadDTO> getPayloadList(String service);

    void savePayload(PayloadDTO reqPayloadDto);

    PayloadDTO updatePayload(PayloadDTO reqPayloadDto);

    PayloadDTO getPayloadInfo(Long id);

    void deletePayload(long id);
    Page<PayloadDTO> searchPayloadList(String searchKey, String searchValue, Pageable pageable);
}
