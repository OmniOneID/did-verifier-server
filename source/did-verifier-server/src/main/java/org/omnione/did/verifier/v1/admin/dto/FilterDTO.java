package org.omnione.did.verifier.v1.admin.dto;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.domain.Payload;
import org.omnione.did.data.model.profile.Filter;

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
}
