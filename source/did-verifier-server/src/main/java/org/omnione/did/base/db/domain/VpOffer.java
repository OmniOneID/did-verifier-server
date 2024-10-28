/*
 * Copyright 2024 OmniOne.
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
 * Entity class representing a Verifiable Presentation (VP) offer in the DID system.
 * This class stores information about VP offers, including their associated service,
 * device, payload, and validity period.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "vp_offer")
public class VpOffer extends BaseEntity implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "offer_id", nullable = false, length = 40)
    private String offerId;
    @Column(name = "service", nullable = false, length = 40)
    private String service;
    @Column(name = "device", nullable = false, length = 40)
    private String device;
    @Column(name = "payload", nullable = false)
    private String payload;
    @Column(name = "passcode", nullable = true, length = 64)
    private String passcode;
    @Column(name = "valid_until", nullable = true)
    private Instant validUntil;
    @Column(name = "vp_policy_id", nullable = false, length = 40)
    private String vpPolicyId;
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;
}