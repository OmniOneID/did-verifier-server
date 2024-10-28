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

import com.fasterxml.jackson.annotation.JsonValue;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;

import java.util.Arrays;

/**
 * Enumeration of symmetric padding types used in cryptographic operations.
 * Represents different padding schemes for symmetric encryption algorithms.
 */
public enum SymmetricPaddingType {
    NOPAD("NOPAD"),
    PKCS5("PKCS5");

    private final String displayName;

    SymmetricPaddingType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    @JsonValue
    public String toString() {
        return displayName;
    }

    /**
     * Converts a display name to the corresponding SymmetricPaddingType.
     *
     * @param displayName The display name to convert.
     * @return The corresponding SymmetricPaddingType.
     * @throws OpenDidException if no matching enum constant is found.
     */
    public static SymmetricPaddingType fromDisplayName(String displayName) {
        return Arrays.stream(SymmetricPaddingType.values())
                .filter(type -> type.displayName.equals(displayName))
                .findFirst()
                .orElseThrow(() -> new OpenDidException(ErrorCode.INVALID_SYMMETRIC_PADDING_TYPE));
    }

    /**
     * Converts this SymmetricPaddingType to the corresponding Omnione SymmetricPaddingType.
     *
     * @return The corresponding Omnione SymmetricPaddingType.
     * @throws OpenDidException if the conversion is not possible.
     */
    public org.omnione.did.crypto.enums.SymmetricPaddingType toOmnioneSymmetricPaddingType() {
        switch (this) {
            case NOPAD:
                return org.omnione.did.crypto.enums.SymmetricPaddingType.NOPAD;
            case PKCS5:
                return org.omnione.did.crypto.enums.SymmetricPaddingType.PKCS5;
            default:
                throw new OpenDidException(ErrorCode.INVALID_SYMMETRIC_PADDING_TYPE);
        }
    }
}