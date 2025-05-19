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
 * Entity class representing a Zero-Knowledge Proof (ZKP) Policy Profile in the DID system.
 * This class stores information about ZKP policy profiles, including their type, title,
 * description, encoding, and language settings, along with an association to a proof request.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "zkp_policy_profile")
public class ZkpPolicyProfile extends BaseEntity implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false, length = 40)
    private String profileId;

    @Column(name = "type", nullable = false, length = 40)
    private String type;

    @Column(name = "title", nullable = false, length = 40)
    private String title;

    @Column(name = "description", nullable = true, length = 200)
    private String description;

    @Column(name = "encoding", nullable = false, length = 40)
    private String encoding;

    @Column(name = "language", nullable = false, length = 40)
    private String language;

    @Column(name = "zkp_proof_request_id", nullable = false)
    private Long zkpProofRequestId;

}