package com.jarvis.deploy.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter(3, 60);
    }

    @Test
    void shouldAllowRequestsWithinLimit() {
        assertTrue(rateLimiter.tryAcquire("prod"));
        assertTrue(rateLimiter.tryAcquire("prod"));
        assertTrue(rateLimiter.tryAcquire("prod"));
    }

    @Test
    void shouldDenyRequestWhenLimitExceeded() {
        rateLimiter.tryAcquire("staging");
        rateLimiter.tryAcquire("staging");
        rateLimiter.tryAcquire("staging");
        assertFalse(rateLimiter.tryAcquire("staging"));
    }

    @Test
    void shouldTrackRemainingPermits() {
        assertEquals(3, rateLimiter.remainingPermits("dev"));
        rateLimiter.tryAcquire("dev");
        assertEquals(2, rateLimiter.remainingPermits("dev"));
        rateLimiter.tryAcquire("dev");
        assertEquals(1, rateLimiter.remainingPermits("dev"));
    }

    @Test
    void shouldIsolateLimitsByKey() {
        rateLimiter.tryAcquire("prod");
        rateLimiter.tryAcquire("prod");
        rateLimiter.tryAcquire("prod");
        assertFalse(rateLimiter.tryAcquire("prod"));
        assertTrue(rateLimiter.tryAcquire("staging"));
    }

    @Test
    void shouldResetLimitForKey() {
        rateLimiter.tryAcquire("prod");
        rateLimiter.tryAcquire("prod");
        rateLimiter.tryAcquire("prod");
        assertFalse(rateLimiter.tryAcquire("prod"));
        rateLimiter.reset("prod");
        assertTrue(rateLimiter.tryAcquire("prod"));
    }

    @Test
    void shouldReturnFullPermitsForUnknownKey() {
        assertEquals(3, rateLimiter.remainingPermits("unknown-env"));
    }

    @Test
    void shouldThrowOnBlankKey() {
        assertThrows(IllegalArgumentException.class, () -> rateLimiter.tryAcquire(""));
        assertThrows(IllegalArgumentException.class, () -> rateLimiter.tryAcquire(null));
    }

    @Test
    void shouldThrowOnInvalidConstructorArgs() {
        assertThrows(IllegalArgumentException.class, () -> new RateLimiter(0, 60));
        assertThrows(IllegalArgumentException.class, () -> new RateLimiter(5, 0));
    }

    @Test
    void shouldExposeConfiguration() {
        assertEquals(3, rateLimiter.getMaxRequests());
        assertEquals(60, rateLimiter.getWindowSeconds());
    }
}
