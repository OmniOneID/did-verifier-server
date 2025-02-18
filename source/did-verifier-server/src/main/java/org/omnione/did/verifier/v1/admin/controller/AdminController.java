package org.omnione.did.verifier.v1.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.constants.UrlConstant;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description...
 *
 * @author : jinhwan-notebook
 * @fileName : AdminController
 * @since : 2025. 2. 18.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = UrlConstant.Verifier.ADMIN)
public class AdminController {

    @Operation(summary = "Request VP Offer QR", description = "Creates a VP Offer QR code")
    @PostMapping(UrlConstant.Verifier.VERIFIER_INFO)
    public String req(){
        return "";
    }


}
