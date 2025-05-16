package org.omnione.did.base.datamodel.data;
import lombok.*;
import org.omnione.did.data.model.did.Proof;
import org.omnione.did.data.model.profile.MetaProfile;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ProofRequestProfile extends MetaProfile {
    private ZkpInnerVerifyProfile profile;
    private Proof proof;
}
