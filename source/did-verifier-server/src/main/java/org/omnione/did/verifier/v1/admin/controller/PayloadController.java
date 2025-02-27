package org.omnione.did.verifier.v1.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.constants.UrlConstant;
import org.omnione.did.base.db.domain.Payload;
import org.omnione.did.verifier.v1.admin.dto.PayloadDTO;
import org.omnione.did.verifier.v1.admin.service.PayloadService;
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
public class PayloadController {
    private final PayloadService payloadService;

    @Operation(summary = "Get Verifier Info", description = "get Verifier Info")
    @GetMapping(UrlConstant.Verifier.GET_VERIFIER_INFO)
    public String getVerifierInfo(){
        return payloadService.getVerifierInfo();
    }

    //Payload Management
    @Operation(summary = "Get Payload List", description = "get Payload(Service) List")
    @GetMapping(UrlConstant.Verifier.GET_PAYLOAD_LIST)
    public List<PayloadDTO> getPayloadList(String service){
        return payloadService.getPayloadList(service);
    }

    //Payload Management
    @Operation(summary = "Get Payload info", description = "get Payload(Service) info")
    @GetMapping(UrlConstant.Verifier.GET_PAYLOAD_INFO)
    public PayloadDTO getPayloadInfo(long id){
        return payloadService.getPayloadInfo(id);
    }

    @Operation(summary = "Save Payload", description = "Save Payload")
    @PostMapping(UrlConstant.Verifier.SAVE_PAYLOAD_INFO)
    public  ResponseEntity<Void> savePayload(@RequestBody PayloadDTO reqPayloadDto){
        payloadService.savePayload(reqPayloadDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete Payload", description = "Delete Payload")
    @DeleteMapping(UrlConstant.Verifier.DELETE_PAYLOAD_INFO)
    public ResponseEntity<Void> deletePayload(long id) {
        payloadService.deletePayload(id);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Update Payload", description = "Update Payload")
    @PutMapping(UrlConstant.Verifier.UPDATE_PAYLOAD_INFO)
    public ResponseEntity<PayloadDTO> updatePayload(@RequestBody PayloadDTO reqPayloadDto) {
        log.info("Updating Payload: {}", reqPayloadDto);

        Payload updatedPayload = payloadService.updatePayload(reqPayloadDto);

        PayloadDTO updatedPayloadDto = new PayloadDTO();
        updatedPayloadDto.setService(updatedPayload.getService());
        updatedPayloadDto.setDevice(updatedPayload.getDevice());
        updatedPayloadDto.setLocked(updatedPayload.isLocked());
        updatedPayloadDto.setMode(updatedPayload.getMode());
        updatedPayloadDto.setEndpoints(updatedPayload.getEndpoints());
        updatedPayloadDto.setValidSecond(updatedPayload.getValidSecond());

        return ResponseEntity.ok(updatedPayloadDto);
    }










}
