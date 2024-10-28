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

package org.omnione.did.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.omnione.did.VerifierApplication;
import org.omnione.did.base.constants.UrlConstant;
import org.omnione.did.base.datamodel.data.AccE2e;
import org.omnione.did.base.datamodel.enums.PresentMode;
import org.omnione.did.common.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.omnione.did.verifier.v1.dto.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = VerifierApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("sample")
class VerifierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Offer QR 요청 테스트")
    void testRequestVpOfferbyQR() throws Exception {
        // 1. 요청 DTO 설정
        RequestOfferReqDto reqDto = new RequestOfferReqDto();
        reqDto.setId("202303241738241234561234ABCD");
        reqDto.setDevice("WEB");
        reqDto.setMode(PresentMode.DIRECT);
        reqDto.setService("login");

        //2. 컨트롤러 호출 및 응답 검증
        MvcResult result = mockMvc.perform(post(UrlConstant.Verifier.V1 + UrlConstant.Verifier.REQUEST_OFFER_QR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print())  // 요청/응답 정보를 로그에 출력
                .andReturn();

        // 3. 실제 응답 확인
        String responseBody = result.getResponse().getContentAsString();
        System.out.println("Actual Controller Response: " + responseBody);

        // 4. 응답 검증 (옵션)
        RequestOfferResDto actualResDto = objectMapper.readValue(responseBody, RequestOfferResDto.class);
        //assertEquals("Success", actualResDto.getResult());
        System.out.println("final  Response: " + actualResDto);

    }
    @Test
    @DisplayName("Profile 요청 테스트")
    void testRequestProfile() throws Exception {
        // 1. 요청 DTO 설정
        RequestProfileReqDto reqDto = new RequestProfileReqDto();
        reqDto.setId("2023113001001156789088888888");
        reqDto.setOfferId("b7c0c81e-748e-4677-90ce-c597bf7ce12d");

        // 2. 컨트롤러 호출 및 응답 검증
        MvcResult result = mockMvc.perform(post(UrlConstant.Verifier.V1 + UrlConstant.Verifier.REQUEST_PROFILE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print())  // 요청/응답 정보를 로그에 출력
                .andReturn();

        // 4. 실제 응답 확인
        String responseBody = result.getResponse().getContentAsString();
        System.out.println("Actual Controller Response: " + responseBody);
        System.out.println("Type: " + result.getResponse().getContentType());

        // 5. 응답 검증 (옵션)
        RequestProfileResDto actualResDto = objectMapper.readValue(responseBody, RequestProfileResDto.class);
        //assertEquals("Success", actualResDto.getResult());
        System.out.println("final  Response: " + actualResDto);
        // 필요한 경우 추가 검증 수행
    }
    @Test
    @DisplayName("Verify 요청 테스트")
    void testRequestVerify() throws Exception {
        // 1. 요청 DTO 설정
        RequestVerifyReqDto reqDto = new RequestVerifyReqDto();
        reqDto.setId("did:example:123456789abcdefghi");
        reqDto.setTxId("99999999-9999-9999-9999-999999999999");
        reqDto.setAccE2e(AccE2e.builder()
                .iv("iv")
                .publicKey("publicKey")
                .proof(null)
                .build());
        reqDto.setEncVp("encVp");
        String jsonStr = "{\"accE2e\":{\"iv\":\"mMqCSuYh1TxcDjDSg2KNoAQ==\",\"proof\":{\"created\":\"2024-09-05T08:46:12Z\",\"proofPurpose\":\"keyAgreement\",\"proofValue\":\"mH+xUj5hhPvvHK0NDtIrkROQh+aoFQ3/dS7nwoEF8hJL8FUusbSj0NJhMkCzOOxMh1qeeOPrlzPeuAWofbRDUudM=\",\"type\":\"Secp256r1Signature2018\",\"verificationMethod\":\"did:omn:c2h8h32CGt8VWXy1R8cHYC7xhGn?versionId=1#keyagree\"},\"publicKey\":\"mAiCdnwHghonB1+r6rlx/PBTPP4XQMyx4aeokMjUJ9TIC\"},\"encVp\":\"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\",\"id\":\"202409051746131240006f5ce786\",\"txId\":\"97db8fde-00af-42f7-8e08-b060ae6dac77\"}";
        JsonUtil.deserializeFromJson(jsonStr, RequestVerifyReqDto.class);

        // 2. 컨트롤러 호출 및 응답 검증
        MvcResult result = mockMvc.perform(post(UrlConstant.Verifier.V1 + UrlConstant.Verifier.REQUEST_VERIFY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print())  // 요청/응답 정보를 로그에 출력
                .andReturn();

        // 3. 실제 응답 확인
        String responseBody = result.getResponse().getContentAsString();
        System.out.println("Actual Controller Response: " + responseBody);

        // 4. 응답 검증 (옵션)
        RequestVerifyResDto actualResDto = objectMapper.readValue(responseBody, RequestVerifyResDto.class);
        //assertEquals("Success", actualResDto.getResult());
        System.out.println("final  Response: " + actualResDto);
        // 필요한 경우 추가 검증 수행
    }
    @Test
    @DisplayName("Verify 확인 테스트")
    void testConfirmVerify() throws Exception {
        // 1. 요청 DTO 설정
        ConfirmVerifyReqDto reqDto = new ConfirmVerifyReqDto();
        reqDto.setOfferId("5e6af61a-96f3-42db-9359-d4299f6e7a8f");

        // 2. 컨트롤러 호출 및 응답 검증
        MvcResult result = mockMvc.perform(post(UrlConstant.Verifier.V1 + UrlConstant.Verifier.CONFIRM_VERIFY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print())  // 요청/응답 정보를 로그에 출력
                .andReturn();

        // 3. 실제 응답 확인
        String responseBody = result.getResponse().getContentAsString();
        System.out.println("Actual Controller Response: " + responseBody);

        // 4. 응답 검증 (옵션)
        ConfirmVerifyResDto actualResDto = objectMapper.readValue(responseBody, ConfirmVerifyResDto.class);
        //assertEquals("Success", actualResDto.getResult());
        System.out.println("final  Response: " + actualResDto);
        // 필요한 경우 추가 검증 수행
    }


}
