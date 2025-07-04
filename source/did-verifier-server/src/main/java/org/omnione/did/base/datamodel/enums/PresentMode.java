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

package org.omnione.did.base.datamodel.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.omnione.did.base.db.constant.ProfileMode;

/**
 * Enumeration of presentation modes in the DID system.
 * Represents different ways in which a Verifiable Presentation can be delivered or processed.
 *
 */
public enum PresentMode {

    DIRECT("Direct"),
    INDIRECT("Indirect"),
    PROXY("Proxy");

    private final String displayName;

    PresentMode(String displayName) {
        this.displayName = displayName;
    }

    @Override
    @JsonValue
    public String toString() {
        return displayName;
    }

    /**
     * Converts a display name to the corresponding PresentMode.
     *
     * @param displayName The display name to convert.
     * @return The corresponding PresentMode.
     * @throws IllegalArgumentException if no matching enum constant is found.
     */
    public static PresentMode fromDisplayName(String displayName) {
        for (PresentMode mode : PresentMode.values()) {
            if (mode.displayName.equals(displayName)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("No enum constant with display name " + displayName);
    }

    public static PresentMode fromProfileMode(ProfileMode profileMode) {
        return switch (profileMode) {
            case Direct -> PresentMode.DIRECT;
            case Indirect -> PresentMode.INDIRECT;
            case Proxy -> PresentMode.PROXY;
        };
    }

}