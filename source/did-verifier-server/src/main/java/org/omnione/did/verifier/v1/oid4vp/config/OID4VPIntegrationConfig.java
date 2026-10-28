/*
 * Copyright 2026 OmniOne.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.omnione.did.verifier.v1.oid4vp.config;

import org.omnione.did.base.db.repository.DcqlScopeMappingRepository;
import org.omnione.did.base.db.repository.Oid4vpConfigRepository;
import org.omnione.did.base.db.repository.Oid4vpSessionJpaRepository;
import org.omnione.did.oid4vc.formatter.oid4vp.verifier.VPTokenVerifier;
import org.omnione.did.oid4vc.formatter.oid4vp.verifier.impl.MDocVPVerifier;
import org.omnione.did.oid4vc.formatter.oid4vp.verifier.impl.OpenDIDVPVerifier;
import org.omnione.did.oid4vc.formatter.oid4vp.verifier.impl.SDJWTVPVerifier;
import org.omnione.did.oid4vc.oid4vp.repository.DCQLScopeMappingRepository;
import org.omnione.did.oid4vc.oid4vp.repository.OID4VPRepository;
import org.omnione.did.oid4vc.oid4vp.repository.SessionRepository;
import org.omnione.did.verifier.v1.oid4vp.adapter.JpaDcqlScopeMappingRepositoryAdapter;
import org.omnione.did.verifier.v1.oid4vp.adapter.JpaOid4vpRepositoryAdapter;
import org.omnione.did.verifier.v1.oid4vp.adapter.JpaSessionRepositoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Registers the JPA-based adapters for the OID4VP SDK repository interfaces.
 * The SDK uses its in-memory repositories only when these beans are missing,
 * which is the case for the sample profile (see {@link Oid4vpSampleConfig}).
 */
@Configuration
public class OID4VPIntegrationConfig {

    @Bean
    @Profile("!sample")
    public OID4VPRepository oid4vpRepository(Oid4vpConfigRepository jpaRepository) {
        return new JpaOid4vpRepositoryAdapter(jpaRepository);
    }

    @Bean
    @Profile("!sample")
    public DCQLScopeMappingRepository sdkDcqlScopeMappingRepository(DcqlScopeMappingRepository jpaRepository) {
        return new JpaDcqlScopeMappingRepositoryAdapter(jpaRepository);
    }

    @Bean
    @Profile("!sample")
    public SessionRepository sdkSessionRepository(Oid4vpSessionJpaRepository jpaRepository) {
        return new JpaSessionRepositoryAdapter(jpaRepository);
    }

    @Bean
    public VPTokenVerifier sdJwtVPVerifier() {
        return new SDJWTVPVerifier();
    }

    @Bean
    public VPTokenVerifier mdocVPVerifier() {
        return new MDocVPVerifier();
    }

    @Bean
    public VPTokenVerifier openDidVPVerifier() {
        return new OpenDIDVPVerifier();
    }
}
