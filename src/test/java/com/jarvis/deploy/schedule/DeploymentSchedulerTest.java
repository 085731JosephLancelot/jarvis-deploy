package com.jarvis.deploy.schedule;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentSchedulerTest {

    private DeploymentScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new DeploymentScheduler();
    }

    @AfterEach
    void tearDown() {
        scheduler.shutdown();
    }

    @Test
    void testScheduleDeploymentInFuture() {
        ScheduledDeployment deployment = new ScheduledDeployment(
                "dep-001", "myapp", "production",
                Instant.now().plusSeconds(60),
                () -> {}
        );
        String id = scheduler.schedule(deployment);
        assertEquals("dep-001", id);
        assertEquals(ScheduleStatus.PENDING, deployment.getStatus());
    }

    @Test
    void testScheduleRejectsNullDeployment() {
        assertThrows(IllegalArgumentException.class, () -> scheduler.schedule(null));
    }

    @Test
    void testScheduleRejectsPastTime() {
        ScheduledDeployment deployment = new ScheduledDeployment(
                "dep-002", "myapp", "staging",
                Instant.now().minusSeconds(10),
                () -> {}
        );
        assertThrows(IllegalArgumentException.class, () -> scheduler.schedule(deployment));
    }

    @Test
    void testCancelPendingDeployment() {
        ScheduledDeployment deployment = new ScheduledDeployment(
                "dep-003", "myapp", "dev",
                Instant.now().plusSeconds(120),
                () -> {}
        );
        scheduler.schedule(deployment);
        boolean cancelled = scheduler.cancel("dep-003");
        assertTrue(cancelled);
        assertEquals(ScheduleStatus.CANCELLED, deployment.getStatus());
    }

    @Test
    void testCancelNonExistentDeploymentReturnsFalse() {
        assertFalse(scheduler.cancel("nonexistent-id"));
    }

    @Test
    void testFindScheduledDeployment() {
        ScheduledDeployment deployment = new ScheduledDeployment(
                "dep-004", "myapp", "qa",
                Instant.now().plusSeconds(60),
                () -> {}
        );
        scheduler.schedule(deployment);
        Optional<ScheduledDeployment> found = scheduler.find("dep-004");
        assertTrue(found.isPresent());
        assertEquals("myapp", found.get().getAppName());
    }

    @Test
    void testListPendingDeployments() {
        scheduler.schedule(new ScheduledDeployment("dep-005", "app1", "prod", Instant.now().plusSeconds(60), () -> {}));
        scheduler.schedule(new ScheduledDeployment("dep-006", "app2", "prod", Instant.now().plusSeconds(90), () -> {}));
        List<ScheduledDeployment> pending = scheduler.listPending();
        assertEquals(2, pending.size());
    }

    @Test
    void testDeploymentExecutesAndCompletesSuccessfully() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean executed = new AtomicBoolean(false);
        ScheduledDeployment deployment = new ScheduledDeployment(
                "dep-007", "myapp", "staging",
                Instant.now().plusMillis(100),
                () -> { executed.set(true); latch.countDown(); }
        );
        scheduler.schedule(deployment);
        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertTrue(executed.get());
        assertEquals(ScheduleStatus.COMPLETED, deployment.getStatus());
    }
}
