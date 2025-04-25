package org.omnione.did.verifier.v1.admin.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ListFeign This class is a Feign client for the TAS API.
 */
@FeignClient(value = "List", url = "${tas.url}", path = "/list/api/v1")
public interface ListFeign {

    @GetMapping("/vcschema/list")
    String requestVcSchemaList();
}
