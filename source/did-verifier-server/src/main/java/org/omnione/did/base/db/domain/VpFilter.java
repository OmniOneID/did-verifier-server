package org.omnione.did.base.db.domain;

import jakarta.persistence.*;
import lombok.*;
import org.omnione.did.base.db.converter.StringListConverter;

import java.io.Serializable;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "vp_filter")
public class VpFilter extends BaseEntity implements Serializable {
    @Id
    @Column(name = "filter_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long filterId;
    @Column(name = "title", nullable = false, length = 40)
    private String title;
    @Column(name = "id", nullable = false, length = 40)
    private String id;
    @Column(name = "type", nullable = false, length = 40)
    private String type;
    @Convert(converter = StringListConverter.class)
    @Column(name = "required_claims")
    private List<String> requiredClaims;
    @Convert(converter = StringListConverter.class)
    @Column(name = "allowed_issuers")
    private List<String> allowedIssuers;
    @Convert(converter = StringListConverter.class)
    @Column(name = "display_claims")
    private List<String> displayClaims;
    @Column(name = "value", nullable = false, length = 2000)
    private String value;
    @Column(name = "present_all", nullable = false)
    private boolean present_all;

}
