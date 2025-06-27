package org.omnione.did.verifier.v1.agent.dto;

import com.google.gson.annotations.Expose;
import lombok.*;
import org.omnione.did.base.datamodel.data.ProofRequestProfile;
import org.omnione.did.data.model.DataObject;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ProofRequestResDto extends DataObject {
    @Expose
    private String txId;
    @Expose
    private ProofRequestProfile proofRequestProfile;


    @Override
    public void fromJson(String s) {

    }
}
