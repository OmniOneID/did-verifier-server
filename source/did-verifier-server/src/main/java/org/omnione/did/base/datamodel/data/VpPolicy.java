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

package org.omnione.did.base.datamodel.data;

import lombok.*;
import org.omnione.did.base.datamodel.enums.OfferType;
import org.omnione.did.base.datamodel.enums.PresentMode;
import org.omnione.did.data.model.DataObject;
import org.omnione.did.data.model.profile.verify.VerifyProfile;
import org.omnione.did.data.model.util.json.GsonWrapper;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Verifiable Presentation (VP) policy.
 * Extends IWObject for JSON serialization/deserialization.
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class VpPolicy extends DataObject {
    private String device;
    private String service;
    private List<String> endpoints;
    private boolean locked;
    private String mode;
    private String policyId;
    private VerifyProfile profile;
    private VerifyOfferPayload payload;

    @Override
    public void fromJson(String val) {
        GsonWrapper gson = new GsonWrapper();
        VpPolicy vpPolicy = gson.fromJson(val, VpPolicy.class);
        this.device = vpPolicy.device;
        this.service = vpPolicy.service;
        this.endpoints = vpPolicy.endpoints;
        this.locked = vpPolicy.locked;
        this.mode = vpPolicy.mode;
        this.policyId = vpPolicy.policyId;
        this.profile = vpPolicy.profile;
    }
}