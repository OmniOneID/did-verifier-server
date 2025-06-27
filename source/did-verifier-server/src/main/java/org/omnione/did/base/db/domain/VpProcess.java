package org.omnione.did.base.db.domain;


import jakarta.persistence.*;
import lombok.*;
import org.omnione.did.base.datamodel.enums.EccCurveType;
import org.omnione.did.base.datamodel.enums.SymmetricCipherType;
import org.omnione.did.base.datamodel.enums.SymmetricPaddingType;
import org.omnione.did.base.db.converter.EccCurveTypeConverter;
import org.omnione.did.base.db.converter.StringListConverter;
import org.omnione.did.base.db.converter.SymmetricCipherTypeConverter;
import org.omnione.did.base.db.converter.SymmetricPaddingTypeConverter;

import java.io.Serializable;
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
    @Convert(converter = EccCurveTypeConverter.class)
    @Column(name = "curve", nullable = false, length = 15)
    private EccCurveType curve;
    @Convert(converter = SymmetricCipherTypeConverter.class)
    @Column(name = "cipher", nullable = false, length = 15)
    private SymmetricCipherType cipher;
    @Convert(converter = SymmetricPaddingTypeConverter.class)
    @Column(name = "padding", nullable = false, length = 15)
    private SymmetricPaddingType padding;

}
