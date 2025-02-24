package org.omnione.did.base.db.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

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
    @Column(name = "required_claims", nullable = false, length = 40)
    private String required_claims;
    @Column(name = "allowed_issuers", nullable = false, length = 40)
    private String allowed_issuers;
    @Column(name = "display_claims", nullable = false, length = 40)
    private String display_claims;
    @Column(name = "value", nullable = false, length = 2000)
    private String value;
    @Column(name = "present_all", nullable = false)
    private boolean present_all;

}
