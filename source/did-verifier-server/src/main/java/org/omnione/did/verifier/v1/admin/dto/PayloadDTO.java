package org.omnione.did.verifier.v1.admin.dto;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.constant.ProfileMode;
import org.omnione.did.base.db.domain.Payload;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * The PayloadDTO class is a data transfer object that is used to transfer payload data between the database and the application.
 * It is designed to facilitate the retrieval of payload data from the database, ensuring that the data is accurate and up-to-date.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayloadDTO {
    private Long id;
    private String payloadId;
    private String service;
    private String device;
    private boolean locked;
    private ProfileMode mode;
    private String endpoints;
    private Integer validSecond;
    private Instant createdAt;
    private Instant updatedAt;

    public PayloadDTO Builder() {
        return PayloadDTO.builder()
                .id(this.id)
                .payloadId(this.payloadId)
                .service(this.service)
                .device(this.device)
                .locked(this.locked)
                .mode(this.mode)
                .endpoints(this.endpoints)
                .validSecond(this.validSecond)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public PayloadDTO toDTO() {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(this, PayloadDTO.class);
    }

    public Payload toEntity() {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(this, Payload.class);
    }

    public static PayloadDTO fromPayload(Payload payload) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return PayloadDTO.builder()
                .id(payload.getId())
                .payloadId(payload.getPayloadId())
                .service(payload.getService())
                .device(payload.getDevice())
                .locked(payload.isLocked())
                .mode(payload.getMode())
                .endpoints(payload.getEndpoints())
                .validSecond(payload.getValidSecond())
                .createdAt(payload.getCreatedAt())
                .updatedAt(payload.getUpdatedAt())
                .build();
    }

    private static String formatInstant(Instant instant, DateTimeFormatter formatter) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter);
    }

}
