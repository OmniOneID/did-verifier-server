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

package org.omnione.did.verifier.v1.protocol.service.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.property.VerifierProperty;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Instant;

@Slf4j
@Component
public class StatusListTokenFetcher {

    // Maximum number of entries (one per status list uri). Real deployments have far fewer issuers/status lists;
    // the limit keeps cache keys derived from a user-controlled credential claim (status.status_list.uri)
    // from accumulating without bound (memory-exhaustion DoS).
    private static final int MAX_CACHE_SIZE = 1000;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RestTemplate restTemplate;
    private final VerifierProperty.StatusListProperties props;
    // The TTL depends on the ttl claim of each status list token and cannot be expressed with Caffeine's global
    // expireAfterWrite, so expiresAt is checked on lookup and maximumSize only bounds the size.
    private final Cache<String, CacheEntry> cache = Caffeine.newBuilder()
            .maximumSize(MAX_CACHE_SIZE)
            .build();

    public StatusListTokenFetcher(RestTemplate restTemplate, VerifierProperty verifierProperty) {
        this.restTemplate = restTemplate;
        this.props = verifierProperty.getStatusList();
    }

    public String fetch(String uri) {
        URI parsed;
        try {
            parsed = URI.create(uri);
        } catch (IllegalArgumentException e) {
            throw new OpenDidException(ErrorCode.STATUS_LIST_FETCH_FAILED);
        }
        String scheme = parsed.getScheme();
        // Enforcing https does not help against SSRF (it changes only the protocol, not the destination). The standard
        // (draft-ietf-oauth-status-list) also states that integrity comes from the Status List Token signature rather than
        // the transport layer. SSRF variants through non-HTTP schemes such as file/gopher are still blocked.
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new OpenDidException(ErrorCode.STATUS_LIST_FETCH_FAILED);
        }
        validateNotPrivateNetwork(parsed);

        CacheEntry cached = cache.getIfPresent(uri);
        if (cached != null && cached.expiresAt.isAfter(Instant.now())) {
            log.debug("Status list cache HIT for uri: {}", uri);
            return cached.jwt;
        }

        log.debug("Status list cache MISS, fetching: {}", uri);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    uri, HttpMethod.GET, null, String.class);
            // The RestTemplate from WebConfig is configured not to follow redirects automatically.
            // A 3xx response (possibly an SSRF bypass attempt) is treated as a failure and is not followed.
            if (response.getStatusCode().is3xxRedirection()) {
                log.warn("Status list host returned a redirect ({}) for uri: {} — not following", response.getStatusCode(), uri);
                throw new OpenDidException(ErrorCode.STATUS_LIST_FETCH_FAILED);
            }
            String jwt = response.getBody();
            if (jwt == null) {
                log.error("Status list token response body is null for uri: {}", uri);
                throw new OpenDidException(ErrorCode.STATUS_LIST_FETCH_FAILED);
            }
            long ttl = parseTtl(jwt);
            cache.put(uri, new CacheEntry(jwt, Instant.now().plusSeconds(ttl)));
            return jwt;
        } catch (OpenDidException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch status list token from {}: {}", uri, e.getMessage());
            throw new OpenDidException(ErrorCode.STATUS_LIST_FETCH_FAILED);
        }
    }

    /**
     * SSRF protection: a scheme check alone does not block internal-network hosts.
     * Resolves the host and rejects it if a resulting IP is loopback, private (RFC1918), link-local (including the
     * 169.254.x.x cloud metadata range), multicast, or wildcard. This does not fully prevent DNS rebinding
     * (the address is not re-validated at request time), but it narrows the exposure considerably compared to a
     * scheme check alone.
     */
    private void validateNotPrivateNetwork(URI uri) {
        String host = uri.getHost();
        if (host == null) {
            throw new OpenDidException(ErrorCode.STATUS_LIST_FETCH_FAILED);
        }
        if (props.getAllowedPrivateHosts().contains(host)) {
            log.debug("Host {} is explicitly allow-listed, skipping private-network check", host);
            return;
        }
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isLoopbackAddress() || address.isSiteLocalAddress()
                        || address.isLinkLocalAddress() || address.isAnyLocalAddress()
                        || address.isMulticastAddress()) {
                    log.warn("Rejected status list uri resolving to a non-public address: {} -> {}. "
                                    + "If this is expected for your deployment (e.g. local/private network), "
                                    + "add \"{}\" to verifier.status-list.allowed-private-hosts in application-verifier.yml.",
                            host, address, host);
                    throw new OpenDidException(ErrorCode.STATUS_LIST_FETCH_FAILED);
                }
            }
        } catch (UnknownHostException e) {
            throw new OpenDidException(ErrorCode.STATUS_LIST_FETCH_FAILED);
        }
    }

    // Extracts the ttl claim from the JWT payload; falls back to minCacheTtlSeconds if it is absent.
    private long parseTtl(String jwt) {
        try {
            com.fasterxml.jackson.databind.JsonNode node = JwtPayloadUtils.parseJwtPayload(OBJECT_MAPPER, jwt);
            long ttl = node.path("ttl").asLong(0);
            if (ttl <= 0) ttl = props.getMinCacheTtlSeconds();
            return Math.min(Math.max(ttl, props.getMinCacheTtlSeconds()), props.getMaxCacheTtlSeconds());
        } catch (Exception e) {
            return props.getMinCacheTtlSeconds();
        }
    }

    private record CacheEntry(String jwt, Instant expiresAt) {}
}
