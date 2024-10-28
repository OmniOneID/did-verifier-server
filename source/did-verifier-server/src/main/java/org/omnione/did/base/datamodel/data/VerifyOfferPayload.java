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
import org.omnione.did.data.model.util.json.GsonWrapper;
import java.util.ArrayList;


/**
 * Payload for Verifiable Presentation offer.
 * Extends IWObject for JSON serialization/deserialization.
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class VerifyOfferPayload extends DataObject {
    private String offerId;
    private OfferType type;
    private PresentMode mode;
    private String device;
    private String service;
    private ArrayList<String> endpoints;
    private String validUntil;
    private Boolean locked;
    @Override
    public void fromJson(String val) {
        GsonWrapper gson = new GsonWrapper();
        VerifyOfferPayload data = gson.fromJson(val, VerifyOfferPayload.class);
        this.offerId = data.offerId;
        this.type = data.type;
        this.mode = data.mode;
        this.device = data.device;
        this.service = data.service;
        this.endpoints = data.endpoints;
        this.validUntil = data.validUntil;
        this.locked = data.locked;
    }
}
