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

package org.omnione.did.verifier.v1.service.sample;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.datamodel.data.VerifyOfferPayload;
import org.omnione.did.base.datamodel.enums.PresentMode;
import org.omnione.did.common.util.JsonUtil;
import org.omnione.did.verifier.v1.dto.*;
import org.omnione.did.verifier.v1.service.VerifierService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Verifier service implementation for handling Verifiable Presentations (VPs) and user verification
 * This is a sample implementation for testing purposes.
 * This implementation does not actually perform any verification, but instead returns dummy data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Profile("sample")
public class VerifierServiceSample implements VerifierService {

    @Override
    public RequestOfferResDto requestVpOfferbyQR(RequestOfferReqDto requestOfferReqDto) {
        VerifyOfferPayload payload = VerifyOfferPayload.builder()
                .service("login")
                .mode(PresentMode.DIRECT)
                .device("WEB")
                .validUntil("2024-12-31T09:00:00Z")
                .locked(false)
                .build();
        return RequestOfferResDto.builder()
                .txId("70e22f4e-ca60-48d3-88ee-a43d42ad3313")
                .payload(payload)
                .build();
    }

    @Override
    public RequestProfileResDto requestProfile(RequestProfileReqDto requestProfileReqDto) {

        String txId = "685c38cb-1e15-4bdb-bc1f-2355b4d845c1";
        String jsonStr = "{\"profile\":{\"description\":\"OpenDID 가입을 위해 제출이 필요한 VP에 대한 프로파일 입니다.\",\"encoding\":\"UTF-8\",\"id\":\"3ab4f3be-82f7-4150-b729-693637d60a59\",\"language\":\"ko\",\"profile\":{\"filter\":{\"credentialSchemas\":[{\"allowedIssuers\":[\"did:omn:issuer\"],\"displayClaims\":[\"testId.aa\"],\"id\":\"http://192.168.3.130:8090/tas/api/v1/vc-schema?name=mdl\",\"requiredClaims\":[\"org.iso.18013.5.birth_date\",\"org.iso.18013.5.family_name\",\"org.iso.18013.5.given_name\"],\"type\":\"OsdSchemaCredential\",\"value\":\"VerifiableProfile\"}]},\"process\":{\"authType\":0,\"endpoints\":[\"http://192.168.3.130:8092/verifier\"],\"reqE2e\":{\"cipher\":\"AES-256-CBC\",\"curve\":\"Secp256r1\",\"nonce\":\"mvMq9QM1y9nYStddUn89B3Q\",\"padding\":\"PKCS5\",\"publicKey\":\"z24pnogvV1HaRMFEkpzEPudGkFcv7KE56ZWz3q7cqi9EWh\"},\"verifierNonce\":\"mvMq9QM1y9nYStddUn89B3Q\"},\"verifier\":{\"certVcRef\":\"http://192.168.3.130:8092/verifier/api/v1/certificate-vc\",\"description\":\"verifier\",\"did\":\"did:omn:verifier\",\"name\":\"verifier\",\"ref\":\"http://192.168.3.130:8092/verifier/api/v1/certificate-vc\"}},\"proof\":{\"created\":\"2024-09-05T17:47:42.098726Z\",\"proofPurpose\":\"assertionMethod\",\"proofValue\":\"z3oY15rx7h4hvE4u8bDuhAj2mV5Rx3dQPrrqXe7CdsmxZcRtVneMYkUZa6257ihuvc4T9FghsePLhnif214Ffce94d\",\"type\":\"Secp256r1Signature2018\",\"verificationMethod\":\"did:omn:verifier?versionId=1#assert\"},\"title\":\"OpenDID 가입 VP 프로파일\",\"type\":\"VerifyProfile\"},\"txId\":\"a9bf02bd-0946-4f0e-9c4c-3193b7ff843f\"}";
        try {
            RequestProfileResDto requestProfileResDto = JsonUtil.deserializeFromJson(jsonStr, RequestProfileResDto.class);
            requestProfileResDto.setTxId(txId);
            return requestProfileResDto;
        } catch (Exception e) {
            log.error("Error deserializing JSON: {}", e.getMessage());
            return null;
        }
    }



    @Override
    public RequestVerifyResDto requestVerify(RequestVerifyReqDto requestVerifyReqDto){

        return RequestVerifyResDto.builder()
                .txId("ce4b6e4c-8563-4101-9453-f5c811a98f73")
                .build();
    }

    @Override
    public ConfirmVerifyResDto confirmVerify(ConfirmVerifyReqDto confirmVerifyReqDto) {
        String JsonStr = "{\"claims\":[{\"caption\":\"Family Name\",\"code\":\"org.iso.18013.5.family_name\",\"format\":\"plain\",\"hideValue\":false,\"type\":\"text\",\"value\":\"Kim\"},{\"caption\":\"Given Name\",\"code\":\"org.iso.18013.5.given_name\",\"format\":\"plain\",\"hideValue\":false,\"type\":\"text\",\"value\":\"Raon\"},{\"caption\":\"Birth date\",\"code\":\"org.iso.18013.5.birth_date\",\"format\":\"plain\",\"hideValue\":false,\"type\":\"text\",\"value\":\"2024-01-01\"}],\"result\":true}\n";
        try {
            return JsonUtil.deserializeFromJson(JsonStr, ConfirmVerifyResDto.class);
        } catch (Exception e) {
            log.error("Error deserializing JSON: {}", e.getMessage());
            return null;
        }
    }
}
