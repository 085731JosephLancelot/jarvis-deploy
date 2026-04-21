package com.jarvis.deploy.quota;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QuotaManagerTest {

    private QuotaManager manager;
    private QuotaPolicy hardPolicy;
    private QuotaPolicy softPolicy;

    @BeforeEach
    void setUp() {
        manager = new QuotaManager();
        hardPolicy = new QuotaPolicy("hard-policy", QuotaScope.ENVIRONMENT, 3, Duration.ofHours(1), true);
        softPolicy = new QuotaPolicy("soft-policy", QuotaScope.USER, 2, Duration.ofHours(1), false);
        manager.registerPolicy(hardPolicy);
        manager.registerPolicy(softPolicy);
    }

    @Test
    void shouldAllowDeploymentWhenUnderQuota() {
        assertTrue(manager.checkQuota("hard-policy", "env-prod"));
    }

    @Test
    void shouldThrowWhenHardLimitExceeded() {
        manager.recordUsage("hard-policy", "env-prod");
        manager.recordUsage("hard-policy", "env-prod");
        manager.recordUsage("hard-policy", "env-prod");
        assertThrows(QuotaExceededException.class, () ->
                manager.checkQuota("hard-policy", "env-prod"));
    }

    @Test
    void shouldReturnFalseWhenSoftLimitExceeded() {
        manager.recordUsage("soft-policy", "user-alice");
        manager.recordUsage("soft-policy", "user-alice");
        assertFalse(manager.checkQuota("soft-policy", "user-alice"));
    }

    @Test
    void shouldTrackUsagePerScopeKey() {
        manager.recordUsage("hard-policy", "env-staging");
        manager.recordUsage("hard-policy", "env-staging");
        assertEquals(2, manager.currentUsage("hard-policy", "env-staging"));
        assertEquals(0, manager.currentUsage("hard-policy", "env-prod"));
    }

    @Test
    void shouldNotInterfereBetweenDifferentScopeKeys() {
        manager.recordUsage("hard-policy", "env-prod");
        manager.recordUsage("hard-policy", "env-prod");
        manager.recordUsage("hard-policy", "env-prod");
        // Different scope key should still be allowed
        assertTrue(manager.checkQuota("hard-policy", "env-staging"));
    }

    @Test
    void shouldThrowForUnknownPolicyOnCheck() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.checkQuota("no-such-policy", "env-x"));
    }

    @Test
    void shouldThrowForUnknownPolicyOnRecord() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.recordUsage("no-such-policy", "env-x"));
    }

    @Test
    void shouldRemovePolicy() {
        manager.removePolicy("soft-policy");
        assertFalse(manager.getPolicy("soft-policy").isPresent());
    }

    @Test
    void shouldListAllPolicyIds() {
        Set<String> ids = manager.listPolicyIds();
        assertTrue(ids.contains("hard-policy"));
        assertTrue(ids.contains("soft-policy"));
        assertEquals(2, ids.size());
    }

    @Test
    void shouldReturnZeroUsageForUnknownPolicy() {
        assertEquals(0, manager.currentUsage("ghost-policy", "env-x"));
    }
}
