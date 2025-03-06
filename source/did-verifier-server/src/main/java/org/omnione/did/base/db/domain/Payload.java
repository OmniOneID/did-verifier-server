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
import org.omnione.did.base.db.constant.ProfileMode;

import java.io.Serializable;
import java.util.ArrayList;


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
@Table(name = "payload")
public class Payload extends BaseEntity implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "payload_id", nullable = false, length = 40)
    private String payloadId;
    @Column(name = "service", nullable = false, length = 40)
    private String service;
    @Column(name = "device", nullable = false, length = 40)
    private String device;
    @Column(name = "locked")
    private boolean locked;
    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 40)
    private ProfileMode mode;
    @Column(name = "endpoints", nullable = false, length = 400)
    private String endpoints;
    @Column(name = "valid_second", nullable = false)
    private Integer validSecond;
}