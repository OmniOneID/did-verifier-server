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

package org.omnione.did.verifier.v1.protocol.service.status;

import lombok.RequiredArgsConstructor;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.property.VerifierProperty;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Set;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@Component
@RequiredArgsConstructor
public class StatusListBitDecoder {

    private static final Set<Integer> SUPPORTED_BITS = Set.of(1, 2, 4, 8);

    private final VerifierProperty verifierProperty;

    public int extract(String lst, int bits, int idx) throws Exception {
        if (!SUPPORTED_BITS.contains(bits)) {
            throw new OpenDidException(ErrorCode.STATUS_LIST_TOKEN_INVALID);
        }
        if (idx < 0) {
            // A negative idx gives a negative bitOffset, and Java masks int shift distances with &31
            // (e.g. idx=-1, bits=2 → bitOffset=-2 → effectively >>30), which bypasses the byteIndex upper-bound check
            // and silently returns 0 (VALID). Reject it explicitly before accessing the array.
            throw new IllegalArgumentException("Status list index must not be negative: " + idx);
        }

        byte[] compressed = JwtPayloadUtils.decodeBase64Url(lst);
        byte[] bytes = decompress(compressed);

        int bitPosition = idx * bits;
        int byteIndex = bitPosition / 8;
        int bitOffset = bitPosition % 8;
        int mask = (1 << bits) - 1;

        if (byteIndex >= bytes.length) {
            throw new IllegalArgumentException(
                "Status list index " + idx + " is out of bounds (list size: " + (bytes.length * 8 / bits) + ")");
        }

        // bits is restricted to {1,2,4,8} above, so the idx*bits range always falls within a single byte.
        // & 0xFF removes sign extension first, defensively avoiding Java's signed byte right-shift issue.
        return ((bytes[byteIndex] & 0xFF) >> bitOffset) & mask;
    }

    // Used by tests to build lst values
    public static String compress(byte[] raw) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (DeflaterOutputStream dos = new DeflaterOutputStream(baos)) {
                dos.write(raw);
            }
            return Base64.getUrlEncoder().withoutPadding().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] decompress(byte[] compressed) throws Exception {
        long maxBytes = verifierProperty.getStatusList().getMaxDecompressedBytes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        long total = 0;
        try (InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(compressed))) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = iis.read(buf)) != -1) {
                total += len;
                if (total > maxBytes) {
                    // Decompression bomb protection: abort as soon as the limit is exceeded during decompression.
                    throw new OpenDidException(ErrorCode.STATUS_LIST_TOKEN_INVALID);
                }
                baos.write(buf, 0, len);
            }
        }
        return baos.toByteArray();
    }
}
