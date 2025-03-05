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
        public static final String REQUEST_OFFER_QR = "/request-offer-qr";
        public static final String REQUEST_VERIFY = "/request-verify";
        public static final String CONFIRM_VERIFY = "/confirm-verify";
        public static final String REQUEST_PROFILE = "/request-profile";

        //ADMIN urls
        public static final String ADMIN = "/verifier/admin/v1";
        //Verifier Manage
        public static final String SAVE_VERIFIER_INFO = "/save_verifier-info";
        public static final String GET_VERIFIER_INFO = "/get_verifier-info";

        //Payload Manage
        public static final String SAVE_PAYLOAD_INFO = "/save_payload-info";
        public static final String GET_PAYLOAD_INFO = "/get_payload-info";
        public static final String GET_PAYLOAD_LIST = "/get_payload-list";
        public static final String DELETE_PAYLOAD_INFO = "/delete_payload-info";
        public static final String UPDATE_PAYLOAD_INFO = "/update_payload-info";

        //Filter Manage
        public static final String SAVE_FILTER_INFO = "/save_filter-info";
        public static final String GET_FILTER_INFO = "/get_filter-info";
        public static final String GET_FILTER_LIST = "/get_filter-list";
        public static final String DELETE_FILTER_INFO = "/delete_filter-info";
        public static final String UPDATE_FILTER_INFO = "/update_filter-info";

        //Process Manage
        public static final String SAVE_PROCESS_INFO = "/save_process-info";
        public static final String GET_PROCESS_INFO = "/get_process-info";
        public static final String GET_PROCESS_LIST = "/get_process-list";
        public static final String DELETE_PROCESS_INFO = "/delete_process-info";
        public static final String UPDATE_PROCESS_INFO = "/update_process-info";


        //Profile Manage
        public static final String SAVE_PROFILE_INFO = "/save_profile-info";
        public static final String GET_PROFILE_INFO = "/get_profile-info";
        public static final String GET_PROFILE_LIST = "/get_profile-list";
        public static final String DELETE_PROFILE_INFO = "/delete_profile-info";
        public static final String UPDATE_PROFILE_INFO = "/update_profile-info";

        //Policy Manage
        public static final String SAVE_POLICY_INFO = "/save_policy-info";
        public static final String GET_POLICY_INFO = "/get_policy-info";
        public static final String GET_POLICY_LIST = "/get_policy-list";
        public static final String DELETE_POLICY_INFO = "/delete_policy-info";
        public static final String UPDATE_POLICY_INFO = "/update_policy-info";


    }
}
