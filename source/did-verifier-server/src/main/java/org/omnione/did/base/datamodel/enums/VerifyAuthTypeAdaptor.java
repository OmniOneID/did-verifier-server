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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * VerifyAuthTypeAdaptor is a GSON TypeAdapter for VerifyAuthType.
 * It serializes and deserializes VerifyAuthType objects to and from JSON.
 */
public class VerifyAuthTypeAdaptor extends TypeAdapter<VerifyAuthType> {
    /**
     * Serializes a VerifyAuthType object to JSON.
     *
     * @param out   The JSON writer to write the VerifyAuthType object to.
     * @param value The VerifyAuthType object to serialize.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(JsonWriter out, VerifyAuthType value) throws IOException {
        if (value != null) {
            out.value(value.getAuthType());
        } else {
            out.nullValue();
        }
    }

    /**
     * Deserializes a VerifyAuthType object from JSON.
     *
     * @param in The JSON reader to read the VerifyAuthType object from.
     * @return The deserialized VerifyAuthType object.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public VerifyAuthType read(JsonReader in) throws IOException {
        int authTypeValue = in.nextInt();
        for (VerifyAuthType authType : VerifyAuthType.values()) {
            if (authType.getAuthType().equals(authTypeValue)) {
                return authType;
            }
        }
        throw new IOException("Unknown authType value: " + authTypeValue);
    }
}
