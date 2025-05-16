package org.omnione.did.verifier.v1.agent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.omnione.did.base.datamodel.data.AccE2e;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RequestVerifyProofReqDto {
    @NotNull(message = "id cannot be null")
    private String id;
    private String txId;
    @Valid
    private AccE2e accE2e;
    @NotNull(message = "encVerifyProof cannot be null")
    private String encProof;

}
