/*
 * Copyright 2026 OmniOne.
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

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "dcql_scope_mapping")
public class DcqlScopeMapping extends BaseEntity implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "scope", nullable = false, length = 100)
    private String scope;
    @Column(name = "dcql_query", nullable = false, columnDefinition = "text")
    private String dcqlQuery;
    @Column(name = "description", length = 500)
    private String description;
    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;
    /**
     * Overrides the clientId scheme. If null, it is determined automatically from the DCQL format
     * ("decentralized_identifier" or "x509_san_dns")
     */
    @Column(name = "client_id_scheme", length = 50)
    private String clientIdScheme;
}
