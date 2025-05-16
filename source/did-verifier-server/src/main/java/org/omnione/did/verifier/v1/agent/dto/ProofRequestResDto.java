package org.omnione.did.verifier.v1.agent.dto;

import lombok.*;
import org.omnione.did.base.datamodel.data.ProofRequestProfile;
import org.opendid.zkp.zkptestcore.datamodel.zkp.ProofRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ProofRequestResDto {
    private String txId;
    private ProofRequestProfile proofRequestProfile;
}
