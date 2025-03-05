package org.omnione.did.verifier.v1.admin.dto;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.domain.Payload;

import java.time.Instant;

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
    private String mode;
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

}
