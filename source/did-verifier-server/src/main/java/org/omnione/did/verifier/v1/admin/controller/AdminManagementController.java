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

import org.omnione.did.base.annotation.ArticleLog;
import org.omnione.did.base.constants.ActionType;
import org.omnione.did.base.constants.ApiType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.constants.UrlConstant;
import org.omnione.did.verifier.v1.admin.dto.*;
import org.omnione.did.verifier.v1.admin.service.AdminManagementService;
import org.omnione.did.verifier.v1.common.dto.EmptyResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = UrlConstant.Verifier.ADMIN)
public class AdminManagementController {
    private final AdminManagementService adminManagementService;

    @ArticleLog(name = "[AdminManagement] Reset Password", apiType = ApiType.ADMIN, targetType = "ADMIN_MANAGEMENT", actionType = ActionType.UPDATE)
    @PostMapping(value = "/admins/reset-password")
    public AdminDto resetPassword(@Valid @RequestBody ResetPasswordReqDto resetPasswordReqDto) {
        return adminManagementService.resetPassword(resetPasswordReqDto);
    }

    @GetMapping(value = "/admins/list")
    public Page<AdminDto> searchAdmins(String searchKey, String searchValue, Pageable pageable) {
        return adminManagementService.searchAdmins(searchKey, searchValue, pageable);
    }

    @GetMapping(value = "/admins")
    public AdminDto getAdmin(@RequestParam Long id) {
        return adminManagementService.findById(id);
    }

    @ArticleLog(name = "[AdminManagement] Register Admin", apiType = ApiType.ADMIN, targetType = "ADMIN_MANAGEMENT", actionType = ActionType.CREATE)
    @PostMapping(value = "/admins")
    public EmptyResDto registerAdmin(@RequestBody RegisterAdminReqDto registerAdminReqDto) {
        return adminManagementService.registerAdmin(registerAdminReqDto);
    }

    @GetMapping(value = "/admins/check-admin-id")
    public VerifyAdminIdUniqueResDto verifyAdminIdUnique(@RequestParam String loginId) {
        return adminManagementService.verifyAdminIdUnique(loginId);
    }

    @ArticleLog(name = "[AdminManagement] Delete Admin", apiType = ApiType.ADMIN, targetType = "ADMIN_MANAGEMENT", actionType = ActionType.DELETE)
    @DeleteMapping(value = "/admins")
    public EmptyResDto deleteAdmin(@RequestParam Long id) {
        return adminManagementService.deleteAdmin(id);
    }

    @ArticleLog(name = "[AdminManagement] Reset Password By Root", apiType = ApiType.ADMIN, targetType = "ADMIN_MANAGEMENT", actionType = ActionType.UPDATE)
    @PostMapping(value = "/admins/root/reset-password")
    public EmptyResDto resetPasswordByRoot(@RequestBody ResetPasswordByRootReqDto resetPasswordByRootReqDto) {
        return adminManagementService.resetPasswordByRoot(resetPasswordByRootReqDto);
    }

    @ArticleLog(name = "[AdminManagement] Change Admin Id And Password", apiType = ApiType.ADMIN, targetType = "ADMIN_MANAGEMENT", actionType = ActionType.UPDATE)
    @PostMapping(value = "/admins/change-id-and-password")
    public AdminDto changeAdminIdAndPassword(@RequestBody ChangeAdminIdAndPasswordReqDto changeAdminIdAndPasswordReqDto) {
        return adminManagementService.changeAdminIdAndPassword(changeAdminIdAndPasswordReqDto);
    }

}
