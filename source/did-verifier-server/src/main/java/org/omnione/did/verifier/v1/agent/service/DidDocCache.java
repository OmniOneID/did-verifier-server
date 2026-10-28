/*
 * Copyright 2025 OmniOne.
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

package org.omnione.did.verifier.v1.agent.service;

import org.omnione.did.data.model.did.DidDocument;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache for storing DID Documents with their corresponding timestamps.
 * This class uses a ConcurrentHashMap to store the cache entries, providing
 * thread-safe access and modifications.
 *
 * The cache entries consist of a DID Document and a timestamp, which are stored
 * in the nested static class CacheEntry.
 *
 * IMPORTANT:
 * - This cache has memory leak protection with MAX_CACHE_SIZE limit
 * - Default TTL (Time To Live) is 1 hour
 * - Old entries are automatically removed when adding new entries if cache is full
 * - For production, consider using Caffeine or Guava Cache with proper TTL and eviction policy
 *
 * Example usage:
 * <pre>
 *     DIDDocCache cache = new DIDDocCache();
 *     cache.putDIDDoc("did:example:123", new DIDDoc());
 *     Object didDoc = cache.getDIDDoc("did:example:123");
 * </pre>
 */
public class DidDocCache {
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    // Cache size limit to prevent memory leak (Phase 2-5 fix)
    private static final int MAX_CACHE_SIZE = 1000;

    // TTL in milliseconds (1 hour)
    private static final long TTL_MILLIS = 3600_000L;

    /**
     * A cache entry for storing a DID Document and its timestamp.
     */
    public static class CacheEntry {
        private DidDocument didDocument;
        private long timestamp;

        public CacheEntry(DidDocument didDocument, long timestamp) {
            this.didDocument = didDocument;
            this.timestamp = timestamp;
        }

        public DidDocument getDidDoc() {
            return didDocument;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Retrieve the DID Document associated with the given DID.
     * Returns null if the entry is expired (TTL exceeded).
     */
    public DidDocument getDidDoc(String did) {
        CacheEntry entry = cache.get(did);
        if (entry == null) {
            return null;
        }

        // Check if entry is expired
        if (isExpired(entry)) {
            cache.remove(did);
            return null;
        }

        return entry.getDidDoc();
    }

    /**
     * Store the given DID Document in the cache with the associated DID.
     * The current timestamp is used as the time of storage.
     *
     * If cache size exceeds MAX_CACHE_SIZE, removes expired entries first.
     * If still over limit, removes oldest entries.
     */
    public void putDidDoc(String did, DidDocument didDoc) {
        // Check cache size limit (Phase 2-5 fix: prevent memory leak)
        if (cache.size() >= MAX_CACHE_SIZE) {
            evictOldEntries();
        }

        cache.put(did, new CacheEntry(didDoc, System.currentTimeMillis()));
    }

    /**
     * Checks if a DID Document is stored in the cache for the given DID.
     */
    public boolean containsDidDoc(String did) {
        return cache.containsKey(did);
    }

    /**
     * Retrieve the timestamp when the DID Document was stored for the given DID.
     *
     */
    public long getTimestamp(String did) {
        CacheEntry entry = cache.get(did);
        return (entry != null) ? entry.getTimestamp() : 0;
    }

    /**
     * Return a set of all DID Documents currently stored in the cache.
     */
    public Set<String> getAllDids() {
        return cache.keySet();
    }

    /**
     * Remove expired entries from the cache (Phase 2-5 fix).
     * Entries older than TTL_MILLIS are removed.
     */
    public void evictExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        cache.entrySet().removeIf(entry ->
            currentTime - entry.getValue().getTimestamp() > TTL_MILLIS
        );
    }

    /**
     * Remove old entries when cache is full (Phase 2-5 fix).
     * First removes expired entries, then removes oldest entries if needed.
     */
    private void evictOldEntries() {
        // First, remove expired entries
        evictExpiredEntries();

        // If still over limit, remove oldest 20% of entries
        if (cache.size() >= MAX_CACHE_SIZE) {
            int toRemove = MAX_CACHE_SIZE / 5; // Remove 20%
            cache.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e1.getValue().getTimestamp(), e2.getValue().getTimestamp()))
                .limit(toRemove)
                .forEach(entry -> cache.remove(entry.getKey()));
        }
    }

    /**
     * Check if a cache entry is expired.
     */
    private boolean isExpired(CacheEntry entry) {
        return System.currentTimeMillis() - entry.getTimestamp() > TTL_MILLIS;
    }

    /**
     * Get current cache size.
     */
    public int size() {
        return cache.size();
    }

    /**
     * Clear all entries in the cache.
     */
    public void clear() {
        cache.clear();
    }

}
