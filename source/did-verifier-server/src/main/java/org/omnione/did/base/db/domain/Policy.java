package org.omnione.did.base.db.domain;

import jakarta.persistence.*;
import lombok.*;

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
    @Column(name = "payload_id", nullable = false, length = 40)
    private String payloadId;
    @Column(name = "policy_profile_id", nullable = false, length = 40)
    private String policyProfileId;
    @Column(name = "policy_title", nullable = false, length = 255)
    private String policyTitle;

}
