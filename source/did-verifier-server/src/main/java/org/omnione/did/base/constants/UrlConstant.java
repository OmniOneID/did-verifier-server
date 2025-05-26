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

package org.omnione.did.base.constants;

/**
 * Verifier URL Constants
 */
public class UrlConstant {

    public static class Verifier {
        public static final String V1 = "/verifier/api/v1";
        public static final String V2 = "/verifier/api/v2";
        public static final String REQUEST_OFFER_QR = "/request-offer-qr";
        public static final String REQUEST_VERIFY = "/request-verify";
        public static final String CONFIRM_VERIFY = "/confirm-verify";
        public static final String REQUEST_PROFILE = "/request-profile";

        //ZKP API
        public static final String REQUEST_PROOF_REQUEST_PROFILE = "/request-proof-request-profile";
        public static final String REQUEST_VERIFY_PROOF = "/request-verify-proof";
        public static final String CONFIRM_VERIFY_PROOF = "/confirm-verify-proof";

        //ADMIN urls
        public static final String ADMIN = "/verifier/admin/v1";
        //Verifier Manage
        public static final String SAVE_VERIFIER_INFO = "/save_verifier-info";

        // Payload Manage
        public static final String SAVE_PAYLOAD_INFO = "/payloads";
        public static final String GET_PAYLOAD_INFO = "/payloads/{id}";
        public static final String GET_PAYLOAD_LIST = "/payloads";
        public static final String DELETE_PAYLOAD_INFO = "/payloads/{id}";
        public static final String UPDATE_PAYLOAD_INFO = "/payloads/{id}";
        public static final String GET_POPUP_PAYLOAD_LIST = "/payloads/popups/{searchValue}";

        // Filter Manage
        public static final String SAVE_FILTER_INFO = "/filters";
        public static final String GET_FILTER_INFO = "/filters/{filterId}";
        public static final String GET_FILTER_LIST = "/filters";
        public static final String DELETE_FILTER_INFO = "/filters/{filterId}";
        public static final String UPDATE_FILTER_INFO = "/filters/{filterId}";
        public static final String GET_POPUP_FILTER_LIST = "/filters/popups/{searchValue}";

        public static final String GET_POPUP_VC_SCHEMAS = "/filters/popups/vc-schemas";

        // Process Manage
        public static final String SAVE_PROCESS_INFO = "/processes";
        public static final String GET_PROCESS_INFO = "/processes/{id}";
        public static final String GET_PROCESS_LIST = "/processes";
        public static final String DELETE_PROCESS_INFO = "/processes/{id}";
        public static final String UPDATE_PROCESS_INFO = "/processes/{id}";
        public static final String GET_POPUP_PROCESS_LIST = "/processes/popups/{searchValue}";

        // Profile Manage
        public static final String SAVE_PROFILE_INFO = "/profiles";
        public static final String GET_PROFILE_INFO = "/profiles/{id}";
        public static final String GET_PROFILE_LIST = "/profiles";
        public static final String DELETE_PROFILE_INFO = "/profiles/{id}";
        public static final String UPDATE_PROFILE_INFO = "/profiles/{id}";
        public static final String GET_POPUP_PROFILE_LIST = "/profiles/popups/{searchValue}";

        // Policy Manage
        public static final String SAVE_POLICY_INFO = "/policies";
        public static final String GET_POLICY_INFO = "/policies/{id}";
        public static final String GET_POLICY_LIST = "/policies";
        public static final String DELETE_POLICY_INFO = "/policies/{id}";
        public static final String UPDATE_POLICY_INFO = "/policies/{id}";
        public static final String GET_ALL_POLICY_LIST = "/policies/all";

        //Verifier Manage
        public static final String GET_VERIFIER_INFO = "/info";

        //Verifier Manage
        public static final String GET_VP_SUBMIT_LIST = "/vpSubmitList";

        //Session
        public static final String LOGIN = "/login";

        //Proof Request urls
        public static final String GET_PROOF_REQUEST = "/zkp/proof-requests";
        public static final String GET_CREDENTIAL_SCHEMA_LIST= "/zkp/proof-requests/credential-schemas";
        public static final String SAVE_PROOF_REQUEST = "/zkp/proof-requests";
        public static final String CHECK_PROOF_REQUEST_NAME = "/zkp/proof-requests/check-name";
        public static final String GET_PROOF_REQUEST_INFO = "/zkp/proof-requests/{id}";
        public static final String UPDATE_PROOF_REQUEST = "/zkp/proof-requests";
        public static final String DELETE_PROOF_REQUEST = "/zkp/proof-requests/{id}";
        public static final String GET_PROOF_REQUEST_ALL = "/zkp/proof-requests/all";

        //ZKp Profile urls
        public static final String GET_ZKP_PROFILE = "/zkp/profiles";
        public static final String SAVE_ZKP_PROFILE = "/zkp/profiles";
        public static final String GET_ZKP_PROFILE_INFO = "/zkp/profiles/{id}";
        public static final String UPDATE_ZKP_PROFILE = "/zkp/profiles/{id}";
        public static final String DELETE_ZKP_PROFILE = "/zkp/profiles/{id}";
        public static final String GET_ZKP_POPUP_PROFILE_LIST = "/zkp/profiles/popups/{searchValue}";
    }
    public static class Tas {
        public static final String ADMIN_V1 = "/tas/admin/v1";
        public static final String REGISTER_DID_PUBLIC = "/entities/register-did/public";
        public static final String REQUEST_ENTITY_STATUS = "/entities/request-status";
    }
}
