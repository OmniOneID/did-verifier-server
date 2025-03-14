package org.omnione.did.verifier.v1.admin.dto;

import lombok.*;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.domain.Policy;

import java.time.Instant;

/**
 * The PolicyDTO class is a data transfer object that is used to transfer policy data between the client and the server.
 * It is designed to facilitate the transfer of policy data, ensuring that the data is accurate and up-to-date.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PolicyDTO {
    private Long id;
    private String policyTitle;
    private String policyId;
    private String payloadId;
    private String payloadService;
    private String profileId;
    private String profileTitle;
    private Instant createdAt;
    private Instant updatedAt;


    public static PolicyDTO toDTO(Policy policy) {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(policy, PolicyDTO.class);
    }
}
