package com.jarvis.deploy.quota;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class QuotaPolicyTest {

    @Test
    void shouldCreatePolicyWithValidParameters() {
        QuotaPolicy policy = new QuotaPolicy("p1", QuotaScope.ENVIRONMENT, 10, Duration.ofHours(1), true);
        assertEquals("p1", policy.getPolicyId());
        assertEquals(QuotaScope.ENVIRONMENT, policy.getScope());
        assertEquals(10, policy.getMaxDeployments());
        assertEquals(Duration.ofHours(1), policy.getWindow());
        assertTrue(policy.isHardLimit());
    }

    @Test
    void shouldRejectZeroMaxDeployments() {
        assertThrows(IllegalArgumentException.class, () ->
                new QuotaPolicy("p2", QuotaScope.GLOBAL, 0, Duration.ofHours(1), false));
    }

    @Test
    void shouldRejectNegativeMaxDeployments() {
        assertThrows(IllegalArgumentException.class, () ->
                new QuotaPolicy("p3", QuotaScope.USER, -5, Duration.ofMinutes(30), true));
    }

    @Test
    void shouldRejectNullPolicyId() {
        assertThrows(NullPointerException.class, () ->
                new QuotaPolicy(null, QuotaScope.SERVICE, 5, Duration.ofHours(1), false));
    }

    @Test
    void toStringShouldContainKeyFields() {
        QuotaPolicy policy = new QuotaPolicy("pol-x", QuotaScope.SERVICE, 3, Duration.ofMinutes(15), false);
        String str = policy.toString();
        assertTrue(str.contains("pol-x"));
        assertTrue(str.contains("SERVICE"));
        assertTrue(str.contains("3"));
    }
}
