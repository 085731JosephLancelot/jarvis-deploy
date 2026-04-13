package com.jarvis.deploy.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RetryPolicyTest {

    @Test
    void defaultPolicy_hasExpectedDefaults() {
        RetryPolicy policy = RetryPolicy.defaultPolicy();
        assertEquals(3, policy.getMaxAttempts());
        assertEquals(Duration.ofSeconds(2), policy.getInitialDelay());
        assertEquals(2.0, policy.getBackoffMultiplier());
        assertEquals(Duration.ofSeconds(30), policy.getMaxDelay());
    }

    @Test
    void noRetry_hasSingleAttempt() {
        RetryPolicy policy = RetryPolicy.noRetry();
        assertEquals(1, policy.getMaxAttempts());
    }

    @Test
    void computeDelay_firstAttempt_returnsInitialDelay() {
        RetryPolicy policy = RetryPolicy.builder()
                .initialDelay(Duration.ofSeconds(2))
                .backoffMultiplier(2.0)
                .maxDelay(Duration.ofMinutes(1))
                .build();
        assertEquals(Duration.ofSeconds(2), policy.computeDelay(0));
    }

    @Test
    void computeDelay_exponentialBackoff_growsCorrectly() {
        RetryPolicy policy = RetryPolicy.builder()
                .initialDelay(Duration.ofSeconds(1))
                .backoffMultiplier(2.0)
                .maxDelay(Duration.ofMinutes(1))
                .build();
        assertEquals(Duration.ofSeconds(1), policy.computeDelay(1));
        assertEquals(Duration.ofSeconds(2), policy.computeDelay(2));
        assertEquals(Duration.ofSeconds(4), policy.computeDelay(3));
    }

    @Test
    void computeDelay_respectsMaxDelay() {
        RetryPolicy policy = RetryPolicy.builder()
                .initialDelay(Duration.ofSeconds(10))
                .backoffMultiplier(3.0)
                .maxDelay(Duration.ofSeconds(20))
                .build();
        Duration delay = policy.computeDelay(5);
        assertTrue(delay.toMillis() <= Duration.ofSeconds(20).toMillis());
    }

    @Test
    void builder_invalidMaxAttempts_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                RetryPolicy.builder().maxAttempts(0).build());
    }

    @Test
    void builder_invalidBackoffMultiplier_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                RetryPolicy.builder().backoffMultiplier(0.5).build());
    }

    @Test
    void builder_customPolicy_configuredCorrectly() {
        RetryPolicy policy = RetryPolicy.builder()
                .maxAttempts(5)
                .initialDelay(Duration.ofMillis(500))
                .backoffMultiplier(1.5)
                .maxDelay(Duration.ofSeconds(10))
                .build();
        assertEquals(5, policy.getMaxAttempts());
        assertEquals(Duration.ofMillis(500), policy.getInitialDelay());
        assertEquals(1.5, policy.getBackoffMultiplier());
        assertEquals(Duration.ofSeconds(10), policy.getMaxDelay());
    }
}
