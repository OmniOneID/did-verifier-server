package org.omnione.did.base.db.domain;

import jakarta.persistence.*;
import lombok.*;
import org.omnione.did.base.db.constant.PolicyType;
import org.omnione.did.base.db.constant.ProtocolType;

import java.io.Serializable;

/**
 * Entity class representing a Verifiable Presentation (VP) policy in the DID system.
 * This class stores information about VP policies, including their associated service,
 * device, payload, and validity period.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "policy")
public class Policy extends BaseEntity implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "policy_id", nullable = false, length = 40)
    private String policyId;
    @Column(name = "payload_id", length = 40)
    private String payloadId;
    @Column(name = "policy_profile_id", length = 40)
    private String policyProfileId;
    @Column(name = "policy_title", nullable = false, length = 255)
    private String policyTitle;
    @Column(name = "policy_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PolicyType policyType = PolicyType.VP;
    @Column(name = "protocol_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProtocolType protocolType = ProtocolType.DID_VP;
    @Column(name = "scope", length = 100)
    private String scope;

}
