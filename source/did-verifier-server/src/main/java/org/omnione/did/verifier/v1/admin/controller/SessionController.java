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

package org.omnione.did.verifier.v1.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.constants.UrlConstant;
import org.omnione.did.verifier.v1.admin.dto.AdminDto;
import org.omnione.did.verifier.v1.admin.dto.RequestAdminLoginReqDto;
import org.omnione.did.verifier.v1.admin.service.SessionService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = UrlConstant.Verifier.ADMIN)
public class SessionController {

    private final SessionService sessionService;

    @PostMapping(UrlConstant.Verifier.LOGIN)
    @ResponseBody
    public AdminDto requestAdminLogin(@Valid @RequestBody RequestAdminLoginReqDto requestAdminLoginReqDto) {
        return sessionService.requestAdminLogin(requestAdminLoginReqDto);
    }
}
