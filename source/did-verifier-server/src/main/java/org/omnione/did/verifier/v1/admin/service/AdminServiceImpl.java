package org.omnione.did.verifier.v1.admin.service;

import lombok.RequiredArgsConstructor;
import org.omnione.did.base.db.domain.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description...
 *
 * @author : jinhwan-notebook
 * @fileName : AdminServiceImpl
 * @since : 2025. 2. 19.
 */
@RequiredArgsConstructor
@Service
public class AdminServiceImpl implements AdminService{

    @Override
    public String getVerifierInfo() {
        return "";
    }

    @Override
    public List<Payload> getPayloadList() {
        return null;
    }

    @Override
    public void savePayload(Payload reqPayload) {

    }

    @Override
    public void deletePayload(String payloadId) {

    }

    @Override
    public Payload updatePayload(Payload reqPayload) {
        return null;
    }
}
