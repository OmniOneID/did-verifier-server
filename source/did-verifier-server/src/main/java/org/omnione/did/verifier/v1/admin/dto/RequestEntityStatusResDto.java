package org.omnione.did.verifier.v1.admin.dto;

import lombok.*;
import org.omnione.did.verifier.v1.admin.constant.EntityStatus;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RequestEntityStatusResDto {
    private EntityStatus status;
}
