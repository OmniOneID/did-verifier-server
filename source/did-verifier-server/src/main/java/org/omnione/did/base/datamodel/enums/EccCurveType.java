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
import org.omnione.did.crypto.enums.DidKeyType;

/**
 * Enumeration of Elliptic Curve Cryptography (ECC) curve types.
 * Represents different elliptic curves used in cryptographic operations.
 *
 */
public enum EccCurveType {
    SECP_256_K1("Secp256k1"),
    SECP_256_R1("Secp256r1");

    private final String displayName;

    EccCurveType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    @JsonValue
    public String toString() {
        return displayName;
    }

    /**
     * Converts a string value to the corresponding EccCurveType.
     *
     * @param value The string representation of the curve type.
     * @return The corresponding EccCurveType.
     * @throws OpenDidException if the input doesn't match any known curve type.
     */
    public static EccCurveType fromValue(String value) {
        for (EccCurveType type : EccCurveType.values()) {
            if (type.displayName.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new OpenDidException(ErrorCode.INVALID_ECC_CURVE_TYPE);
    }

    /**
     * Converts this EccCurveType to the corresponding Omnione EccCurveType.
     *
     * @return The corresponding Omnione EccCurveType.
     * @throws OpenDidException if the conversion is not possible.
     */
    public org.omnione.did.crypto.enums.EccCurveType toOmnioneEccCurveType() {
        switch (this) {
            case SECP_256_K1:
                return org.omnione.did.crypto.enums.EccCurveType.Secp256k1;
            case SECP_256_R1:
                return org.omnione.did.crypto.enums.EccCurveType.Secp256r1;
            default:
                throw new OpenDidException(ErrorCode.INVALID_ECC_CURVE_TYPE);
        }
    }

    /**
     * Converts this EccCurveType to the corresponding Omnione DidKeyType.
     *
     * @return The corresponding Omnione DidKeyType.
     */
    public DidKeyType toOmnioneDidKeyType() {
        switch (this) {
            case SECP_256_K1:
                return DidKeyType.SECP256K1_VERIFICATION_KEY_2018;
            case SECP_256_R1:
                return DidKeyType.SECP256R1_VERIFICATION_KEY_2018;
            default:
                return DidKeyType.RSA_VERIFICATION_KEY_2018;
        }
    }
}