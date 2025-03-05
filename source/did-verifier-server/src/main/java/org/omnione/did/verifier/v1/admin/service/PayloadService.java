package org.omnione.did.verifier.v1.admin.service;

import org.omnione.did.verifier.v1.admin.dto.PayloadDTO;

import java.util.List;


public interface PayloadService {


    String getVerifierInfo();

    List<PayloadDTO> getPayloadList(String service);

    void savePayload(PayloadDTO reqPayloadDto);

    PayloadDTO updatePayload(PayloadDTO reqPayloadDto);

    PayloadDTO getPayloadInfo(long id);

    void deletePayload(long id);
}
