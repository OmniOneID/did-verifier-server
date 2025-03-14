package org.omnione.did.verifier.v1.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.domain.VpFilter;
import org.omnione.did.data.model.profile.Filter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * The FilterDTO class is a data transfer object that is used to transfer filter data between the database and the application.
 * It is designed to facilitate the retrieval of filter data from the database, ensuring that the data is accurate and up-to-date.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilterDTO {
    private Long filterId;
    private String title;
    private String id;
    private String type;
    private List<String> requiredClaims;
    private List<String> allowedIssuers;
    private List<String> displayClaims;
    private String value;
    private boolean presentAll;
    private String createdAt;

    public FilterDTO build() {
        return FilterDTO.builder()
                .filterId(this.filterId)
                .title(this.title)
                .id(this.id)
                .type(this.type)
                .requiredClaims(this.requiredClaims)
                .allowedIssuers(this.allowedIssuers)
                .displayClaims(this.displayClaims)
                .value(this.value)
                .presentAll(this.presentAll)
                .createdAt(this.createdAt)
                .build();
    }

    public FilterDTO toDTO() {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(this, FilterDTO.class);
    }

    public Filter toEntity() {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(this, Filter.class);
    }
    public static FilterDTO fromVpFilter(VpFilter filter) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return FilterDTO.builder()
                .filterId(filter.getFilterId())
                .title(filter.getTitle())
                .id(filter.getId())
                .type(filter.getType())
                .requiredClaims(filter.getRequiredClaims())
                .allowedIssuers(filter.getAllowedIssuers())
                .displayClaims(filter.getDisplayClaims())
                .value(filter.getValue())
                .presentAll(filter.isPresent_all())
                .createdAt(formatInstant(filter.getCreatedAt(), formatter))
                .build();
    }

    private static String formatInstant(@NotNull Instant instant, DateTimeFormatter formatter) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter);
    }
}
