package org.omnione.did.verifier.v1.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.constants.UrlConstant;
import org.omnione.did.base.db.domain.Payload;
import org.omnione.did.verifier.v1.admin.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * The AdminController class provides methods for saving and getting verifier information.
 */

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = UrlConstant.Verifier.ADMIN)
public class AdminController {

    private final AdminService adminService;


    @Operation(summary = "Get Verifier Info", description = "get Verifier Info to config File")
    @GetMapping(UrlConstant.Verifier.GET_VERIFIER_INFO)
    public String getVerifierInfo(){
        return adminService.getVerifierInfo();
    }

    //Payload Management
    @Operation(summary = "Get Payload List", description = "get Payload List")
    @GetMapping(UrlConstant.Verifier.GET_PAYLOAD_INFO)
    public List<Payload> getPayloadList(){
        return adminService.getPayloadList();
    }

    @Operation(summary = "Save Payload", description = "Save Payload")
    @PostMapping(UrlConstant.Verifier.SAVE_PAYLOAD_INFO)
    public  ResponseEntity<Void> savePayload(@RequestBody Payload payload){
        adminService.savePayload(payload);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete Payload", description = "Delete Payload")
    @PostMapping(UrlConstant.Verifier.DELETE_PAYLOAD_INFO)
    public ResponseEntity<Void> deletePayload(String payloadId) {
        adminService.deletePayload(payloadId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update Payload", description = "Update Payload")
    @PostMapping(UrlConstant.Verifier.UPDATE_PAYLOAD_INFO)
    public ResponseEntity<Payload> updatePayload(@RequestBody Payload payload) {
        Payload updatedPayload = adminService.updatePayload(payload);
        return ResponseEntity.ok(updatedPayload);
    }












}
