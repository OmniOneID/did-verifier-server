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


/**
 * Entity class representing a Zero-Knowledge Proof (ZKP) Request in the DID system.
 * This class stores information about ZKP proof requests, including requested attributes,
 * predicates, and cryptographic parameters.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "zkp_proof_request")
public class ZkpProofRequest extends BaseEntity implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 40)
    private String name;

    @Column(name = "version", nullable = false, length = 10)
    private String version;

    @Column(name = "requested_attributes", nullable = true)
    private String requestedAttributes;

    @Column(name = "requested_predicates", nullable = true)
    private String requestedPredicates;

    @Column(name = "curve", nullable = false, length = 40)
    private String curve;

    @Column(name = "cipher", nullable = false, length = 40)
    private String cipher;

    @Column(name = "padding", nullable = false, length = 40)
    private String padding;
}