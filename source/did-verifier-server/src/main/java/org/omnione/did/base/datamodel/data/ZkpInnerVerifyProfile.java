package org.omnione.did.base.datamodel.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.*;
import org.omnione.did.data.model.profile.ReqE2e;
import org.omnione.did.data.model.provider.ProviderDetail;
import org.omnione.did.zkp.datamodel.proofrequest.ProofRequest;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ZkpInnerVerifyProfile {
    @SerializedName("verifier")
    @Expose
    private ProviderDetail verifier;
    @SerializedName("proofRequest")
    @Expose
    private ProofRequest proofRequest;
    @SerializedName("reqE2e")
    @Expose
    private ReqE2e reqE2e;
}
