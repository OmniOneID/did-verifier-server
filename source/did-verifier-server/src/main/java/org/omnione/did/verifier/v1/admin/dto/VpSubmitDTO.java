package org.omnione.did.verifier.v1.admin.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.omnione.did.base.db.domain.VpSubmit;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VpSubmitDTO {
    private Long id;
    private String vp;
    private String holderDID;
    private Long transactionId;
    private String txId;
    private String transactionStatus;
    private String createdAt;

    public VpSubmitDTO Builder() {
        return VpSubmitDTO.builder()
                .id(this.id)
                .vp(this.vp)
                .transactionId(this.transactionId)
                .build();
    }

    public static VpSubmitDTO fromVpSubmit(VpSubmit vpSubmit) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return VpSubmitDTO.builder()
                .id(vpSubmit.getId())
                .vp(vpSubmit.getVp())
                .transactionId(vpSubmit.getTransactionId())
                .createdAt(formatInstant(vpSubmit.getCreatedAt(), formatter))
                .build();
    }

    public static String formatInstant(@NotNull Instant instant, DateTimeFormatter formatter) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter);
    }
}
