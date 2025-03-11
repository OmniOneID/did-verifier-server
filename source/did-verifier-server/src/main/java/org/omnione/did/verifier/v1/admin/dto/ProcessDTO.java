package org.omnione.did.verifier.v1.admin.dto;

import lombok.*;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.datamodel.enums.EccCurveType;
import org.omnione.did.base.datamodel.enums.SymmetricCipherType;
import org.omnione.did.base.datamodel.enums.SymmetricPaddingType;
import org.omnione.did.base.db.domain.VpProcess;
import org.omnione.did.data.model.profile.verify.VerifyProcess;
import java.util.List;

/**
 * The ProcessDTO class is a data transfer object that is used to transfer process data between the database and the application.
 * It is designed to facilitate the retrieval of process data from the database, ensuring that the data is accurate and up-to-date.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessDTO {
    private Long id;
    private String title;
    private ReqE2e reqE2e;
    private int authType;
    private List<String> endpoints;
    private String verifierNonce;

    @Setter
    @Getter
    public static class ReqE2e {
        private EccCurveType curve;
        private String nonce;
        private SymmetricCipherType cipher;
        private SymmetricPaddingType padding;
        private String publicKey;

    }

    public ProcessDTO Builder() {
        return ProcessDTO.builder()
                .id(this.id)
                .title(this.title)
                .reqE2e(this.reqE2e)
                .authType(this.authType)
                .endpoints(this.endpoints)
                .verifierNonce(this.verifierNonce)
                .build();
    }

    public VpProcess toEntity() {
        VpProcess process = new VpProcess();
        process.setId(this.id);
        process.setTitle(this.title);
        process.setCurve(this.reqE2e.getCurve());
        process.setCipher(this.reqE2e.getCipher());
        process.setPadding(this.reqE2e.getPadding());
        process.setAuthType(this.authType);
        process.setEndpoints(this.endpoints);
        return process;
    }


    public VerifyProcess toVerifyProcessEntity() {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(this, VerifyProcess.class);
    }

    public static ProcessDTO fromEntity(VpProcess vpProcess) {
        if (vpProcess == null) {
            return null;
        }

        // Create ReqE2e inner object
        ProcessDTO.ReqE2e reqE2e = new ProcessDTO.ReqE2e();
        reqE2e.setCurve(vpProcess.getCurve());
        reqE2e.setCipher(vpProcess.getCipher());
        reqE2e.setPadding(vpProcess.getPadding());

        // Create and populate ProcessDTO
        return ProcessDTO.builder()
                .title(vpProcess.getTitle())
                .id(vpProcess.getId())
                .reqE2e(reqE2e)
                .authType(vpProcess.getAuthType())
                .endpoints(vpProcess.getEndpoints())
                .build();
    }
    
}
