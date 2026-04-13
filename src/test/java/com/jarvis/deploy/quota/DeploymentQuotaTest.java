package com.jarvis.deploy.quota;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentQuotaTest {

    private DeploymentQuota quota;

    @BeforeEach
    void setUp() {
        quota = new DeploymentQuota("production", 3, 3600);
    }

    @Test
    void constructor_validArgs_createsQuota() {
        assertEquals("production", quota.getEnvironment());
        assertEquals(3, quota.getMaxDeployments());
        assertEquals(3600, quota.getWindowSeconds());
        assertEquals(0, quota.getUsedDeployments());
        assertEquals(3, quota.getRemainingDeployments());
    }

    @Test
    void constructor_blankEnvironment_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> new DeploymentQuota("  ", 3, 3600));
    }

    @Test
    void constructor_zeroMaxDeployments_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> new DeploymentQuota("staging", 0, 3600));
    }

    @Test
    void constructor_negativeWindowSeconds_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> new DeploymentQuota("staging", 5, -1));
    }

    @Test
    void canDeploy_withinQuota_returnsTrue() {
        assertTrue(quota.canDeploy());
    }

    @Test
    void recordDeployment_incrementsUsed() {
        quota.recordDeployment();
        assertEquals(1, quota.getUsedDeployments());
        assertEquals(2, quota.getRemainingDeployments());
    }

    @Test
    void recordDeployment_upToLimit_succeeds() {
        quota.recordDeployment();
        quota.recordDeployment();
        quota.recordDeployment();
        assertEquals(3, quota.getUsedDeployments());
        assertEquals(0, quota.getRemainingDeployments());
        assertFalse(quota.canDeploy());
    }

    @Test
    void recordDeployment_exceedsLimit_throwsQuotaExceededException() {
        quota.recordDeployment();
        quota.recordDeployment();
        quota.recordDeployment();
        assertThrows(QuotaExceededException.class, () -> quota.recordDeployment());
    }

    @Test
    void quotaExceededException_containsEnvironmentInfo() {
        quota.recordDeployment();
        quota.recordDeployment();
        quota.recordDeployment();
        QuotaExceededException ex = assertThrows(QuotaExceededException.class,
            () -> quota.recordDeployment());
        assertTrue(ex.getMessage().contains("production"));
        assertTrue(ex.getMessage().contains("3/3"));
    }

    @Test
    void toString_containsRelevantInfo() {
        String result = quota.toString();
        assertTrue(result.contains("production"));
        assertTrue(result.contains("3"));
    }

    @Test
    void equals_sameEnvironment_returnsTrue() {
        DeploymentQuota other = new DeploymentQuota("production", 5, 7200);
        assertEquals(quota, other);
    }

    @Test
    void equals_differentEnvironment_returnsFalse() {
        DeploymentQuota other = new DeploymentQuota("staging", 3, 3600);
        assertNotEquals(quota, other);
    }
}
