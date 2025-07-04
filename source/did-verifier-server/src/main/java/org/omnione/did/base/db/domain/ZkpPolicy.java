/*
 * Copyright 2025 OmniOne.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.omnione.did.base.db.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * Entity class representing a Zero-Knowledge Proof (ZKP) Policy in the DID system.
 * This class stores information about ZKP policies, including their associated payload
 * and profile references.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "zkp_policy")
public class ZkpPolicy extends BaseEntity implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_id", nullable = false, length = 40)
    private String policyId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "payload_id", nullable = false, length = 40)
    private String payloadId;

    @Column(name = "profile_id", nullable = false, length = 40)
    private String profileId;


    //@ManyToOne
    //@JoinColumn(name = "payload_id", referencedColumnName = "payload_id", insertable = false, updatable = false)
    //private Payload payload;

    //@ManyToOne
    //@JoinColumn(name = "profile_id", referencedColumnName = "zkp_profile_id", insertable = false, updatable = false)
    //private ZkpPolicyProfile zkpPolicyProfile;
}