package org.omnione.did.base.datamodel.data;

import lombok.*;
import org.omnione.did.data.model.profile.ReqE2e;
import org.omnione.did.data.model.provider.ProviderDetail;
import org.opendid.zkp.zkptestcore.datamodel.zkp.ProofRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ZkpInnerVerifyProfile {
    private ProviderDetail verifier;
    private ProofRequest proofRequest;
    private ReqE2e reqE2e;
}
