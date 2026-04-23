package com.jarvis.deploy.concurrency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.jarvis.deploy.concurrency.ConcurrencyManager.AcquireResult.*;
import static org.junit.jupiter.api.Assertions.*;

class ConcurrencyManagerTest {

    private ConcurrencyManager manager;

    @BeforeEach
    void setUp() {
        manager = new ConcurrencyManager();
    }

    @Test
    void acquireWithoutPolicy_alwaysSucceeds() {
        assertEquals(ACQUIRED, manager.tryAcquire("dev"));
        assertEquals(ACQUIRED, manager.tryAcquire("dev"));
        assertEquals(2, manager.getActiveCount("dev"));
    }

    @Test
    void strictPolicy_rejectsSecondDeployment() {
        manager.registerPolicy(ConcurrencyPolicy.strict("prod"));

        assertEquals(ACQUIRED, manager.tryAcquire("prod"));
        assertEquals(REJECTED, manager.tryAcquire("prod"));
        assertEquals(1, manager.getActiveCount("prod"));
    }

    @Test
    void strictPolicy_allowsAfterRelease() {
        manager.registerPolicy(ConcurrencyPolicy.strict("prod"));

        assertEquals(ACQUIRED, manager.tryAcquire("prod"));
        manager.release("prod");
        assertEquals(ACQUIRED, manager.tryAcquire("prod"));
    }

    @Test
    void queuedPolicy_queuesWhenFull() {
        manager.registerPolicy(ConcurrencyPolicy.queued("staging", 2, 3));

        assertEquals(ACQUIRED, manager.tryAcquire("staging"));
        assertEquals(ACQUIRED, manager.tryAcquire("staging"));
        assertEquals(QUEUED,   manager.tryAcquire("staging"));
        assertEquals(QUEUED,   manager.tryAcquire("staging"));
        assertEquals(2, manager.getActiveCount("staging"));
        assertEquals(2, manager.getQueuedCount("staging"));
    }

    @Test
    void queuedPolicy_rejectsWhenQueueFull() {
        manager.registerPolicy(ConcurrencyPolicy.queued("staging", 1, 1));

        assertEquals(ACQUIRED, manager.tryAcquire("staging"));
        assertEquals(QUEUED,   manager.tryAcquire("staging"));
        assertEquals(REJECTED, manager.tryAcquire("staging"));
    }

    @Test
    void cancelOldestPolicy_grantsSlotImmediately() {
        ConcurrencyPolicy policy = new ConcurrencyPolicy(
                "canary", 1,
                ConcurrencyPolicy.OverflowStrategy.CANCEL_OLDEST, 0);
        manager.registerPolicy(policy);

        assertEquals(ACQUIRED,      manager.tryAcquire("canary"));
        assertEquals(CANCEL_OLDEST, manager.tryAcquire("canary"));
    }

    @Test
    void release_doesNotGoBelowZero() {
        manager.registerPolicy(ConcurrencyPolicy.strict("qa"));
        manager.release("qa");
        assertEquals(0, manager.getActiveCount("qa"));
    }

    @Test
    void hasPolicy_returnsTrueOnlyWhenRegistered() {
        assertFalse(manager.hasPolicy("unknown"));
        manager.registerPolicy(ConcurrencyPolicy.strict("prod"));
        assertTrue(manager.hasPolicy("prod"));
    }

    @Test
    void concurrencyPolicy_strict_throwsOnInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> ConcurrencyPolicy.strict(null));
        assertThrows(IllegalArgumentException.class,
                () -> ConcurrencyPolicy.strict(""));
    }

    @Test
    void concurrencyPolicy_queued_throwsOnNegativeLimits() {
        assertThrows(IllegalArgumentException.class,
                () -> ConcurrencyPolicy.queued("staging", -1, 3));
        assertThrows(IllegalArgumentException.class,
                () -> ConcurrencyPolicy.queued("staging", 1, -1));
    }
}
