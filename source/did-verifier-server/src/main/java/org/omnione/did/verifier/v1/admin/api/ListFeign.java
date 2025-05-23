package org.omnione.did.verifier.v1.admin.api;

import org.omnione.did.verifier.v1.admin.api.dto.ListCredentialSchemaDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * ListFeign This class is a Feign client for the TAS API.
 */
@FeignClient(value = "List", url = "${tas.url}")
public interface ListFeign {
    @GetMapping("/list/api/v1/vcschema/list")
    String requestVcSchemaList();

    @GetMapping("/list/admin/v1/credential-schemas/all")
    List<ListCredentialSchemaDto> requestCredentialSchemaList();
}
