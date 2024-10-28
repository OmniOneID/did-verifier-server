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

package org.omnione.did.verifier.v1.controller;

import lombok.RequiredArgsConstructor;
import org.omnione.did.base.constants.UrlConstant;
import org.omnione.did.data.model.vc.VerifiableCredential;
import org.omnione.did.verifier.v1.dto.EnrollEntityResDto;
import org.omnione.did.verifier.v1.service.CertificateVcService;
import org.omnione.did.verifier.v1.service.EnrollEntityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The EnrollEntityController class provides methods for enrolling entities and requesting certificate VCs.
 * It is designed to facilitate the enrollment of entities and the retrieval of certificate VCs, ensuring that the data is accurate and up-to-date.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(value = UrlConstant.Verifier.V1)
public class EnrollEntityController {
    private final EnrollEntityService enrollEntityService;
    private final CertificateVcService certificateVcService;

    /**
     * Enrolls an entity.
     *
     * @return EnrollEntityResDto empty response.
     */
    @PostMapping("/certificate-vc")
    public EnrollEntityResDto enrollEntity() {
        return enrollEntityService.enrollEntity();
    }

    /**
     * Request a verifiable credential (VC) from the CertificateVc database.
     * This method retrieves the latest VC from the database and returns it as a VerifiableCredential object.
     *
     * @return VerifiableCredential object containing the requested VC
     */
    @GetMapping("/certificate-vc")
    public String requestCertificateVc() {
        return certificateVcService.requestCertificateVc();
    }
}
