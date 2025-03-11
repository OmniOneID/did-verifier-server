package org.omnione.did.base.db.domain;


import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.checker.units.qual.C;
import org.hibernate.annotations.Type;
import org.omnione.did.base.datamodel.enums.EccCurveType;
import org.omnione.did.base.datamodel.enums.SymmetricCipherType;
import org.omnione.did.base.datamodel.enums.SymmetricPaddingType;
import org.omnione.did.base.db.converter.StringListConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "vp_process")
public class VpProcess extends BaseEntity implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "title", nullable = false, length = 40)
    private String title;
    @Convert(converter = StringListConverter.class)
    @Column(name = "endpoints")
    private List<String> endpoints;
    @Column(name = "auth_type")
    private int authType;
    @Column(name = "curve", nullable = false, length = 40)
    private EccCurveType curve;
    @Column(name = "cipher", nullable = false, length = 40)
    private SymmetricCipherType cipher;
    @Column(name = "padding", nullable = false, length = 40)
    private SymmetricPaddingType padding;

}
