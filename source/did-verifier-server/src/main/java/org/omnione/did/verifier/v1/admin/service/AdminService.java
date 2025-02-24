package org.omnione.did.verifier.v1.admin.service;

import org.omnione.did.base.db.domain.Payload;

import java.util.List;

/**
 * Description...
 *
 * @author : jinhwan-notebook
 * @fileName : AdminService
 * @since : 2025. 2. 19.
 */
public interface AdminService {


    String getVerifierInfo();


    List<Payload> getPayloadList();

    void savePayload(Payload reqPayload);

    void deletePayload(String payloadId);

    Payload updatePayload(Payload reqPayload);
}
