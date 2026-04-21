package com.jarvis.deploy.checkpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CheckpointRegistryTest {

    private CheckpointRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CheckpointRegistry();
    }

    @Test
    void shouldRegisterAndFindCheckpointById() {
        DeploymentCheckpoint cp = new DeploymentCheckpoint("cp-1", "smoke-test", "deploy-42");
        registry.register(cp);

        Optional<DeploymentCheckpoint> found = registry.findById("cp-1");
        assertTrue(found.isPresent());
        assertEquals("smoke-test", found.get().getName());
    }

    @Test
    void shouldReturnEmptyForUnknownId() {
        assertTrue(registry.findById("nonexistent").isEmpty());
    }

    @Test
    void shouldFindCheckpointsByDeploymentId() {
        registry.register(new DeploymentCheckpoint("cp-1", "stage-1", "deploy-10"));
        registry.register(new DeploymentCheckpoint("cp-2", "stage-2", "deploy-10"));
        registry.register(new DeploymentCheckpoint("cp-3", "stage-1", "deploy-99"));

        List<DeploymentCheckpoint> results = registry.findByDeploymentId("deploy-10");
        assertEquals(2, results.size());
    }

    @Test
    void shouldReturnAllPassedWhenAllPassedOrSkipped() {
        DeploymentCheckpoint cp1 = new DeploymentCheckpoint("cp-1", "health", "deploy-5");
        DeploymentCheckpoint cp2 = new DeploymentCheckpoint("cp-2", "smoke", "deploy-5");
        cp1.pass("healthy");
        cp2.skip("not applicable");
        registry.register(cp1);
        registry.register(cp2);

        assertTrue(registry.allPassed("deploy-5"));
    }

    @Test
    void shouldReturnFalseForAllPassedWhenOneFailed() {
        DeploymentCheckpoint cp1 = new DeploymentCheckpoint("cp-1", "health", "deploy-6");
        DeploymentCheckpoint cp2 = new DeploymentCheckpoint("cp-2", "smoke", "deploy-6");
        cp1.pass("ok");
        cp2.fail("timeout");
        registry.register(cp1);
        registry.register(cp2);

        assertFalse(registry.allPassed("deploy-6"));
        assertTrue(registry.anyFailed("deploy-6"));
    }

    @Test
    void shouldClearCheckpointsForDeployment() {
        registry.register(new DeploymentCheckpoint("cp-1", "stage-1", "deploy-7"));
        registry.register(new DeploymentCheckpoint("cp-2", "stage-2", "deploy-7"));
        registry.register(new DeploymentCheckpoint("cp-3", "stage-1", "deploy-8"));

        registry.clearForDeployment("deploy-7");

        assertEquals(1, registry.size());
        assertTrue(registry.findByDeploymentId("deploy-7").isEmpty());
    }

    @Test
    void shouldFindByStatus() {
        DeploymentCheckpoint cp1 = new DeploymentCheckpoint("cp-1", "check-a", "deploy-9");
        DeploymentCheckpoint cp2 = new DeploymentCheckpoint("cp-2", "check-b", "deploy-9");
        cp1.pass("all good");
        registry.register(cp1);
        registry.register(cp2);

        List<DeploymentCheckpoint> pending = registry.findByStatus(CheckpointStatus.PENDING);
        assertEquals(1, pending.size());
        assertEquals("cp-2", pending.get(0).getId());
    }

    @Test
    void shouldThrowOnNullRegistration() {
        assertThrows(NullPointerException.class, () -> registry.register(null));
    }
}
