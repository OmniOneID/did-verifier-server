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
@Table(name = "vp_process")
public class VpProcess extends BaseEntity implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "endpoints", nullable = false, length = 40)
    private String endpoints;
    @Column(name = "auth_type", nullable = false, length = 40)
    private String authType;
    @Column(name = "nonce", nullable = false, length = 40)
    private String nonce;
    @Column(name = "curve", nullable = false, length = 40)
    private String curve;
    @Column(name = "public_key", nullable = false, length = 40)
    private String publicKey;
    @Column(name = "cipher", nullable = false, length = 40)
    private String cipher;
    @Column(name = "padding", nullable = false, length = 40)
    private String padding;

}
