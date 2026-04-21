package com.jarvis.deploy.drain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DrainManagerTest {

    private DrainManager manager;
    private DrainPolicy policy;

    @BeforeEach
    void setUp() {
        manager = new DrainManager();
        policy = new DrainPolicy("production", Duration.ofSeconds(30), 3, true);
        manager.registerPolicy(policy);
    }

    @Test
    void initialStateIsIdle() {
        assertEquals(DrainManager.DrainState.IDLE, manager.getState("production"));
    }

    @Test
    void beginDrainTransitionsToDraining() {
        manager.beginDrain("production");
        assertEquals(DrainManager.DrainState.DRAINING, manager.getState("production"));
    }

    @Test
    void completeDrainTransitionsToDrained() {
        manager.beginDrain("production");
        manager.completeDrain("production");
        assertEquals(DrainManager.DrainState.DRAINED, manager.getState("production"));
    }

    @Test
    void isReadyToDeployAfterDrained() {
        manager.beginDrain("production");
        manager.completeDrain("production");
        assertTrue(manager.isReadyToDeploy("production"));
    }

    @Test
    void isNotReadyToDeployWhileDraining() {
        manager.beginDrain("production");
        assertFalse(manager.isReadyToDeploy("production"));
    }

    @Test
    void resetRestoresIdleState() {
        manager.beginDrain("production");
        manager.reset("production");
        assertEquals(DrainManager.DrainState.IDLE, manager.getState("production"));
    }

    @Test
    void beginDrainTwiceThrows() {
        manager.beginDrain("production");
        assertThrows(IllegalStateException.class, () -> manager.beginDrain("production"));
    }

    @Test
    void completeDrainWithoutBeginThrows() {
        assertThrows(IllegalStateException.class, () -> manager.completeDrain("production"));
    }

    @Test
    void unknownEnvironmentThrows() {
        assertThrows(IllegalArgumentException.class, () -> manager.getState("staging"));
    }

    @Test
    void registerPolicyWithNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> manager.registerPolicy(null));
    }

    @Test
    void drainPolicyRejectsBlankEnvironment() {
        assertThrows(IllegalArgumentException.class,
                () -> new DrainPolicy("", Duration.ofSeconds(10), 1, false));
    }

    @Test
    void drainPolicyRejectsZeroTimeout() {
        assertThrows(IllegalArgumentException.class,
                () -> new DrainPolicy("staging", Duration.ZERO, 1, false));
    }

    @Test
    void drainPolicyEqualityAndHashCode() {
        DrainPolicy p1 = new DrainPolicy("staging", Duration.ofSeconds(10), 2, false);
        DrainPolicy p2 = new DrainPolicy("staging", Duration.ofSeconds(10), 2, false);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }
}
