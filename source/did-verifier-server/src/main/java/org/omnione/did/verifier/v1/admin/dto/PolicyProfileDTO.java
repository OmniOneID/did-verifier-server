package org.omnione.did.verifier.v1.admin.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.omnione.did.base.db.domain.PolicyProfile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * The ProfileDTO class is a data transfer object that is used to transfer profile data between the database and the application.
 * It is designed to facilitate the retrieval of profile data from the database, ensuring that the data is accurate and up-to-date.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PolicyProfileDTO {
    private long id;
    private String policyProfileId;
    private String type;
    private String title;
    private String description;
    private LogoImage logo;
    private Verifier verifier;
    private String encoding;
    private String language;
    private long processId;
    private long filterId;
    private String filterTitle;
    private String processTitle;
    private String createdAt;


    @Setter
    @Getter
    public static class LogoImage {
        private String format;
        private String link;
        private String value;

        public LogoImage(String format, String link, String value) {
            this.format = format;
            this.link = link;
            this.value = value;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Verifier {
        private String did;
        private String certVcRef;
        private String name;
        private String ref;
    }

    public PolicyProfile makeEntity() {
        PolicyProfile profile = new PolicyProfile();
        profile.setId(this.id);
        profile.setPolicyProfileId(UUID.randomUUID().toString());
        profile.setType(this.type);
        profile.setTitle(this.title);
        profile.setDescription(this.description);
        profile.setEncoding(this.encoding);
        profile.setLanguage(this.language);
        profile.setProcessId(this.processId);
        profile.setFilterId(this.filterId);
        if (this.logo != null) {
            profile.setFormat(this.logo.getFormat());
            profile.setLink(this.logo.getLink());
            profile.setValue(this.logo.getValue());
        }
        return profile;
    }

    public static PolicyProfileDTO fromEntity(PolicyProfile policyProfile) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        PolicyProfileDTO dto = new PolicyProfileDTO();
        dto.setId(policyProfile.getId());
        dto.setPolicyProfileId(policyProfile.getPolicyProfileId());
        dto.setType(policyProfile.getType());
        dto.setTitle(policyProfile.getTitle());
        dto.setDescription(policyProfile.getDescription());
        dto.setEncoding(policyProfile.getEncoding());
        dto.setLanguage(policyProfile.getLanguage());
        dto.setProcessId(policyProfile.getProcessId());
        dto.setFilterId(policyProfile.getFilterId());
        dto.setCreatedAt(formatInstant(policyProfile.getCreatedAt(), formatter));
        return dto;
    }
    private static String formatInstant(@NotNull Instant instant, DateTimeFormatter formatter) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter);
    }



}
