package org.omnione.did.verifier.v1.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.omnione.did.base.db.constant.VerifierStatus;
import org.omnione.did.base.db.domain.VerifierInfo;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.data.model.did.DidDocument;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class VerifierInfoResDto {
    private Long id;
    private String did;
    private String name;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private VerifierStatus status;
    private String serverUrl;
    private String certificateUrl;
    private Map<String, Object> didDocument;
    private String createdAt;
    private String updatedAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static String formatInstant(Instant instant) {
        return Optional.ofNullable(instant)
                .map(FORMATTER::format)
                .orElse(null);
    }

    public static VerifierInfoResDto fromEntity(VerifierInfo verifierInfo) {
        return Optional.ofNullable(verifierInfo)
                .map(t -> VerifierInfoResDto.builder()
                        .id(t.getId())
                        .did(t.getDid())
                        .name(t.getName())
                        .status(t.getStatus())
                        .serverUrl(t.getServerUrl())
                        .certificateUrl(t.getCertificateUrl())
                        .createdAt(formatInstant(t.getCreatedAt()))
                        .updatedAt(formatInstant(t.getUpdatedAt()))
                        .build())
                .orElse(null);
    }

    public static VerifierInfoResDto fromEntity(VerifierInfo verifierInfo, DidDocument didDocument) {
        return Optional.ofNullable(verifierInfo)
                .map(t -> VerifierInfoResDto.builder()
                        .id(t.getId())
                        .did(t.getDid())
                        .name(t.getName())
                        .status(t.getStatus())
                        .serverUrl(t.getServerUrl())
                        .certificateUrl(t.getCertificateUrl())
                        .didDocument(parseDidDocToMap(didDocument.toJson()))
                        .createdAt(formatInstant(t.getCreatedAt()))
                        .updatedAt(formatInstant(t.getUpdatedAt()))
                        .build())
                .orElse(null);
    }

    public static Map<String, Object> parseDidDocToMap(String didDocJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(didDocJson, Map.class);
        } catch (JsonProcessingException e) {
            throw new OpenDidException(ErrorCode.INVALID_DID_DOCUMENT);
        } catch (Exception e) {
            throw new OpenDidException(ErrorCode.INVALID_DID_DOCUMENT);
        }
    }
}
