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
@Table(name = "vp_policy_profile")
public class VpPolicyProfile extends BaseEntity implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "policy_profile_id", nullable = false, length = 40)
    private String policyProfileId;
    @Column(name = "type", nullable = false, length = 40)
    private String type;
    @Column(name = "title", nullable = false, length = 40)
    private String title;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "encoding", nullable = false, length = 40)
    private String encoding;
    @Column(name = "language", nullable = false, length = 40)
    private String language;
    @Column(name = "format", nullable = false, length = 40)
    private String format;
    @Column(name = "link", nullable = false, length = 40)
    private String link;
    @Column(name = "value", nullable = false)
    private String value;
    @Column(name = "process_id", nullable = false)
    private Long processId;
    @Column(name = "filter_id", nullable = false)
    private Long filterId;

}
