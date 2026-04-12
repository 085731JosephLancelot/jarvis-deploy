package com.jarvis.deploy.lock;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentLockTest {

    @Test
    void shouldCreateLockWithValidArguments() {
        DeploymentLock lock = new DeploymentLock("production", "alice", "dep-001");

        assertEquals("production", lock.getEnvironmentName());
        assertEquals("alice", lock.getAcquiredBy());
        assertEquals("dep-001", lock.getDeploymentId());
        assertNotNull(lock.getAcquiredAt());
    }

    @Test
    void shouldRecordAcquiredAtTimeOnCreation() {
        Instant before = Instant.now();
        DeploymentLock lock = new DeploymentLock("staging", "bob", "dep-002");
        Instant after = Instant.now();

        assertFalse(lock.getAcquiredAt().isBefore(before));
        assertFalse(lock.getAcquiredAt().isAfter(after));
    }

    @Test
    void shouldThrowWhenEnvironmentNameIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeploymentLock("", "alice", "dep-001"));
    }

    @Test
    void shouldThrowWhenEnvironmentNameIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeploymentLock(null, "alice", "dep-001"));
    }

    @Test
    void shouldThrowWhenAcquiredByIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeploymentLock("production", "  ", "dep-001"));
    }

    @Test
    void shouldThrowWhenDeploymentIdIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeploymentLock("production", "alice", null));
    }

    @Test
    void shouldBeEqualWhenEnvironmentAndDeploymentIdMatch() {
        DeploymentLock lock1 = new DeploymentLock("production", "alice", "dep-001");
        DeploymentLock lock2 = new DeploymentLock("production", "bob", "dep-001");

        assertEquals(lock1, lock2);
        assertEquals(lock1.hashCode(), lock2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDeploymentIdDiffers() {
        DeploymentLock lock1 = new DeploymentLock("production", "alice", "dep-001");
        DeploymentLock lock2 = new DeploymentLock("production", "alice", "dep-002");

        assertNotEquals(lock1, lock2);
    }

    @Test
    void toStringShouldContainKeyFields() {
        DeploymentLock lock = new DeploymentLock("staging", "charlie", "dep-099");
        String str = lock.toString();

        assertTrue(str.contains("staging"));
        assertTrue(str.contains("charlie"));
        assertTrue(str.contains("dep-099"));
    }
}
