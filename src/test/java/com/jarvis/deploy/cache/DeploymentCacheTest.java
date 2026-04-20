package com.jarvis.deploy.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentCacheTest {

    private DeploymentCache cache;

    @BeforeEach
    void setUp() {
        cache = new DeploymentCache(Duration.ofMinutes(5));
    }

    @Test
    void shouldStoreAndRetrieveValue() {
        cache.put("deploy:v1.2.3", "artifact-payload");
        Optional<Object> result = cache.get("deploy:v1.2.3");
        assertTrue(result.isPresent());
        assertEquals("artifact-payload", result.get());
    }

    @Test
    void shouldReturnEmptyForMissingKey() {
        Optional<Object> result = cache.get("nonexistent");
        assertFalse(result.isPresent());
    }

    @Test
    void shouldExpireEntryAfterTtl() throws InterruptedException {
        DeploymentCache shortTtlCache = new DeploymentCache(Duration.ofMillis(50));
        shortTtlCache.put("key", "value");
        assertTrue(shortTtlCache.contains("key"));
        Thread.sleep(100);
        assertFalse(shortTtlCache.contains("key"));
    }

    @Test
    void shouldInvalidateSingleEntry() {
        cache.put("env:prod", "config-data");
        cache.invalidate("env:prod");
        assertFalse(cache.contains("env:prod"));
    }

    @Test
    void shouldInvalidateAllEntries() {
        cache.put("k1", "v1");
        cache.put("k2", "v2");
        cache.invalidateAll();
        assertEquals(0, cache.size());
    }

    @Test
    void shouldRejectNullOrBlankKey() {
        assertThrows(IllegalArgumentException.class, () -> cache.put(null, "value"));
        assertThrows(IllegalArgumentException.class, () -> cache.put("  ", "value"));
    }

    @Test
    void shouldRejectInvalidTtl() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentCache(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new DeploymentCache(Duration.ofSeconds(-1)));
        assertThrows(IllegalArgumentException.class, () -> new DeploymentCache(null));
    }

    @Test
    void shouldOverwriteExistingEntry() {
        cache.put("key", "old-value");
        cache.put("key", "new-value");
        assertEquals("new-value", cache.get("key").orElseThrow());
    }

    @Test
    void shouldReportCorrectSizeAfterEviction() throws InterruptedException {
        DeploymentCache shortTtlCache = new DeploymentCache(Duration.ofMillis(50));
        shortTtlCache.put("a", 1);
        shortTtlCache.put("b", 2);
        Thread.sleep(100);
        assertEquals(0, shortTtlCache.size());
    }
}
