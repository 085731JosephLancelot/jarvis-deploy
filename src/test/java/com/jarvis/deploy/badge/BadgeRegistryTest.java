package com.jarvis.deploy.badge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BadgeRegistryTest {

    private BadgeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new BadgeRegistry();
    }

    @Test
    void shouldRegisterAndRetrieveBadge() {
        DeploymentBadge badge = new DeploymentBadge("svc-auth", "production", "1.0.0");
        registry.register(badge);

        Optional<DeploymentBadge> result = registry.get("svc-auth", "production");
        assertTrue(result.isPresent());
        assertEquals("svc-auth", result.get().getServiceId());
        assertEquals("production", result.get().getEnvironment());
    }

    @Test
    void shouldReturnEmptyForMissingBadge() {
        Optional<DeploymentBadge> result = registry.get("svc-missing", "staging");
        assertFalse(result.isPresent());
    }

    @Test
    void shouldUpdateOrCreateBadge() {
        DeploymentBadge badge = registry.updateOrCreate("svc-api", "staging", BadgeStatus.SUCCESS, "2.1.0");

        assertNotNull(badge);
        assertEquals(BadgeStatus.SUCCESS, badge.getStatus());
        assertEquals("2.1.0", badge.getVersion());
        assertEquals(1, registry.size());
    }

    @Test
    void shouldUpdateExistingBadgeOnSecondCall() {
        registry.updateOrCreate("svc-api", "staging", BadgeStatus.IN_PROGRESS, "2.0.0");
        DeploymentBadge updated = registry.updateOrCreate("svc-api", "staging", BadgeStatus.SUCCESS, "2.1.0");

        assertEquals(BadgeStatus.SUCCESS, updated.getStatus());
        assertEquals("2.1.0", updated.getVersion());
        assertEquals(1, registry.size());
    }

    @Test
    void shouldReturnAllBadges() {
        registry.register(new DeploymentBadge("svc-a", "dev", "1.0"));
        registry.register(new DeploymentBadge("svc-b", "dev", "1.1"));
        registry.register(new DeploymentBadge("svc-a", "prod", "1.0"));

        List<DeploymentBadge> all = registry.getAll();
        assertEquals(3, all.size());
    }

    @Test
    void shouldFilterBadgesByEnvironment() {
        registry.register(new DeploymentBadge("svc-a", "dev", "1.0"));
        registry.register(new DeploymentBadge("svc-b", "dev", "1.1"));
        registry.register(new DeploymentBadge("svc-a", "prod", "1.0"));

        List<DeploymentBadge> devBadges = registry.getByEnvironment("dev");
        assertEquals(2, devBadges.size());
        devBadges.forEach(b -> assertEquals("dev", b.getEnvironment()));
    }

    @Test
    void shouldRemoveBadge() {
        registry.register(new DeploymentBadge("svc-x", "qa", "3.0"));
        boolean removed = registry.remove("svc-x", "qa");

        assertTrue(removed);
        assertFalse(registry.get("svc-x", "qa").isPresent());
    }

    @Test
    void shouldReturnFalseWhenRemovingNonExistentBadge() {
        boolean removed = registry.remove("svc-none", "prod");
        assertFalse(removed);
    }

    @Test
    void shouldDefaultToUnknownStatus() {
        DeploymentBadge badge = new DeploymentBadge("svc-new", "uat", "0.1");
        assertEquals(BadgeStatus.UNKNOWN, badge.getStatus());
    }
}
