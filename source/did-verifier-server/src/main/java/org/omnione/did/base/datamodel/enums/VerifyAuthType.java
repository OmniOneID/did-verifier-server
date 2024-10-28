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

package org.omnione.did.base.datamodel.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Enumeration of authentication types for verification in the DID system.
 * Represents different levels and methods of authentication that can be required during verification.
 */
@Getter
public enum VerifyAuthType {
    NO_RESTRICTIONS_AUTHENTICATION(0x00000000),
    NO_AUTHENTICATION(0x00000001),
    PIN(0x00000002),
    BIO(0x00000004),
    PIN_OR_BIO(0x00000006),
    PIN_AND_BIO(0x00008006);

    private final int value;

    VerifyAuthType(int value) {
        this.value = value;
    }

    @JsonValue
    public Integer getAuthType() {
        return value;
    }

    /**
     * Creates a VerifyAuthType from its integer value.
     *
     * @param value The integer value of the authentication type.
     * @return The corresponding VerifyAuthType.
     * @throws IllegalArgumentException if no matching enum constant is found.
     */
    public static VerifyAuthType fromInteger(int value) {
        for (VerifyAuthType type : VerifyAuthType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant with value " + value);
    }
}