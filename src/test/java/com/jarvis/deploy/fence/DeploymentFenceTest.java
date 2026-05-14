package com.jarvis.deploy.fence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentFenceTest {

    private DeploymentFence fence;

    @BeforeEach
    void setUp() {
        fence = new DeploymentFence("fence-001", "prod-guard");
    }

    @Test
    void testInitialStateIsActive() {
        assertTrue(fence.isActive());
    }

    @Test
    void testPermitsAllWhenNoRestrictionsSet() {
        assertTrue(fence.permits("staging", "auth-service"));
        assertTrue(fence.permits("production", "payment-service"));
    }

    @Test
    void testAllowedEnvironmentRestrictsOthers() {
        fence.allowEnvironment("production");
        assertTrue(fence.permits("production", "auth-service"));
        assertFalse(fence.permits("staging", "auth-service"));
    }

    @Test
    void testMultipleAllowedEnvironments() {
        fence.allowEnvironment("production");
        fence.allowEnvironment("staging");
        assertTrue(fence.permits("production", "svc-a"));
        assertTrue(fence.permits("staging", "svc-a"));
        assertFalse(fence.permits("dev", "svc-a"));
    }

    @Test
    void testBlockedServiceDeniesPermission() {
        fence.blockService("legacy-service");
        assertFalse(fence.permits("production", "legacy-service"));
        assertTrue(fence.permits("production", "new-service"));
    }

    @Test
    void testAllowedEnvironmentAndBlockedServiceCombined() {
        fence.allowEnvironment("production");
        fence.blockService("dangerous-service");
        assertFalse(fence.permits("staging", "safe-service"));
        assertFalse(fence.permits("production", "dangerous-service"));
        assertTrue(fence.permits("production", "safe-service"));
    }

    @Test
    void testDeactivatedFenceDeniesAll() {
        fence.deactivate();
        assertFalse(fence.isActive());
        assertFalse(fence.permits("production", "any-service"));
    }

    @Test
    void testGettersReturnCorrectValues() {
        assertEquals("fence-001", fence.getFenceId());
        assertEquals("prod-guard", fence.getName());
        assertNotNull(fence.getCreatedAt());
    }

    @Test
    void testAllowedEnvironmentsIsUnmodifiable() {
        fence.allowEnvironment("production");
        assertThrows(UnsupportedOperationException.class,
                () -> fence.getAllowedEnvironments().add("staging"));
    }

    @Test
    void testNullFenceIdThrows() {
        assertThrows(NullPointerException.class, () -> new DeploymentFence(null, "name"));
    }

    @Test
    void testNullNameThrows() {
        assertThrows(NullPointerException.class, () -> new DeploymentFence("id", null));
    }

    @Test
    void testToStringContainsFenceId() {
        assertTrue(fence.toString().contains("fence-001"));
    }
}
