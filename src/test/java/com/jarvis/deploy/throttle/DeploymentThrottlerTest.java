package com.jarvis.deploy.throttle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentThrottlerTest {

    private DeploymentThrottler throttler;

    @BeforeEach
    void setUp() {
        throttler = new DeploymentThrottler();
    }

    @Test
    void shouldAllowDeploymentWhenNoPolicyRegistered() {
        ThrottleResult result = throttler.tryAcquire("staging");
        assertTrue(result.isAllowed());
        assertEquals(ThrottleResult.Status.ALLOWED, result.getStatus());
    }

    @Test
    void shouldAllowDeploymentWithinLimit() {
        throttler.registerPolicy(new ThrottlePolicy("staging", 3, Duration.ofMinutes(5), true));
        ThrottleResult result = throttler.tryAcquire("staging");
        assertTrue(result.isAllowed());
        assertFalse(result.isDenied());
    }

    @Test
    void shouldDenyDeploymentWhenHardLimitExceeded() {
        throttler.registerPolicy(new ThrottlePolicy("prod", 2, Duration.ofMinutes(10), true));
        throttler.tryAcquire("prod");
        throttler.tryAcquire("prod");
        ThrottleResult result = throttler.tryAcquire("prod");
        assertTrue(result.isDenied());
        assertEquals(ThrottleResult.Status.DENIED, result.getStatus());
    }

    @Test
    void shouldWarnInsteadOfDenyWhenSoftLimit() {
        throttler.registerPolicy(new ThrottlePolicy("dev", 1, Duration.ofMinutes(1), false));
        throttler.tryAcquire("dev");
        ThrottleResult result = throttler.tryAcquire("dev");
        assertEquals(ThrottleResult.Status.WARNED, result.getStatus());
        assertTrue(result.isAllowed());
        assertFalse(result.isDenied());
    }

    @Test
    void shouldTrackDeploymentCount() {
        throttler.registerPolicy(new ThrottlePolicy("staging", 5, Duration.ofMinutes(5), true));
        throttler.tryAcquire("staging");
        throttler.tryAcquire("staging");
        assertEquals(2, throttler.getDeploymentCount("staging"));
    }

    @Test
    void shouldReturnZeroCountForUnknownEnvironment() {
        assertEquals(0, throttler.getDeploymentCount("unknown"));
    }

    @Test
    void shouldRemovePolicyAndReset() {
        throttler.registerPolicy(new ThrottlePolicy("staging", 1, Duration.ofMinutes(5), true));
        assertTrue(throttler.hasPolicy("staging"));
        throttler.removePolicy("staging");
        assertFalse(throttler.hasPolicy("staging"));
        ThrottleResult result = throttler.tryAcquire("staging");
        assertTrue(result.isAllowed());
    }

    @Test
    void shouldRejectNegativeMaxDeployments() {
        assertThrows(IllegalArgumentException.class,
                () -> new ThrottlePolicy("prod", 0, Duration.ofMinutes(5), true));
    }

    @Test
    void shouldRejectNullEnvironmentInPolicy() {
        assertThrows(NullPointerException.class,
                () -> new ThrottlePolicy(null, 3, Duration.ofMinutes(5), true));
    }
}
