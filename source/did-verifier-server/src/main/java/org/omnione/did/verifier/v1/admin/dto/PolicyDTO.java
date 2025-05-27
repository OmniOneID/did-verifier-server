package org.omnione.did.verifier.v1.admin.dto;

import lombok.*;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.constant.PolicyType;
import org.omnione.did.base.db.domain.Policy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
    private String policyProfileId;
    private String profileTitle;
    private String createdAt;
    private PolicyType policyType;


    public static PolicyDTO toDTO(Policy policy) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return PolicyDTO.builder()
                .id(policy.getId())
                .policyId(policy.getPolicyId())
                .payloadId(policy.getPayloadId())
                .policyProfileId(policy.getPolicyProfileId())
                .policyTitle(policy.getPolicyTitle())
                .policyType(policy.getPolicyType())
                .createdAt(formatInstant(policy.getCreatedAt(), formatter))
                .build();
    }

    public static PolicyDTO toDTO(Policy policy, String payloadService, String profileTitle) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return PolicyDTO.builder()
                .id(policy.getId())
                .policyId(policy.getPolicyId())
                .payloadId(policy.getPayloadId())
                .policyProfileId(policy.getPolicyProfileId())
                .policyTitle(policy.getPolicyTitle())
                .policyType(policy.getPolicyType())
                .payloadService(payloadService)
                .profileTitle(profileTitle)
                .createdAt(formatInstant(policy.getCreatedAt(), formatter))
                .build();
    }

    private static String formatInstant(Instant instant, DateTimeFormatter formatter) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter);
    }
}
