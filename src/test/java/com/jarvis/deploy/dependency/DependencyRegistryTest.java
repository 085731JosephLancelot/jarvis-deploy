package com.jarvis.deploy.dependency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DependencyRegistryTest {

    private DependencyRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DependencyRegistry();
    }

    @Test
    void shouldRegisterDependencySuccessfully() {
        DeploymentDependency dep = new DeploymentDependency("dep-1", "deploy-B", "deploy-A", DependencyType.REQUIRES_SUCCESS);
        registry.register(dep);
        assertEquals(1, registry.size());
        assertTrue(registry.findById("dep-1").isPresent());
    }

    @Test
    void shouldRejectNullDependency() {
        assertThrows(NullPointerException.class, () -> registry.register(null));
    }

    @Test
    void shouldReturnDependenciesForDeployment() {
        registry.register(new DeploymentDependency("dep-1", "deploy-B", "deploy-A", DependencyType.REQUIRES_SUCCESS));
        registry.register(new DeploymentDependency("dep-2", "deploy-B", "deploy-C", DependencyType.REQUIRES_RUNNING));
        List<DeploymentDependency> deps = registry.getDependenciesFor("deploy-B");
        assertEquals(2, deps.size());
    }

    @Test
    void shouldReturnDependentsOfDeployment() {
        registry.register(new DeploymentDependency("dep-1", "deploy-B", "deploy-A", DependencyType.REQUIRES_SUCCESS));
        registry.register(new DeploymentDependency("dep-2", "deploy-C", "deploy-A", DependencyType.REQUIRES_COMPLETION));
        List<DeploymentDependency> dependents = registry.getDependentsOf("deploy-A");
        assertEquals(2, dependents.size());
    }

    @Test
    void shouldRemoveDependency() {
        registry.register(new DeploymentDependency("dep-1", "deploy-B", "deploy-A", DependencyType.REQUIRES_SUCCESS));
        assertTrue(registry.remove("dep-1"));
        assertFalse(registry.findById("dep-1").isPresent());
        assertEquals(0, registry.size());
    }

    @Test
    void shouldReturnFalseWhenRemovingNonExistentDependency() {
        assertFalse(registry.remove("nonexistent"));
    }

    @Test
    void shouldRejectSelfDependency() {
        assertThrows(IllegalArgumentException.class, () ->
            new DeploymentDependency("dep-1", "deploy-A", "deploy-A", DependencyType.REQUIRES_SUCCESS));
    }

    @Test
    void shouldDetectCycleAndThrow() {
        registry.register(new DeploymentDependency("dep-1", "deploy-B", "deploy-A", DependencyType.REQUIRES_SUCCESS));
        registry.register(new DeploymentDependency("dep-2", "deploy-C", "deploy-B", DependencyType.REQUIRES_SUCCESS));
        DeploymentDependency cyclic = new DeploymentDependency("dep-3", "deploy-A", "deploy-C", DependencyType.REQUIRES_SUCCESS);
        assertThrows(IllegalStateException.class, () -> registry.register(cyclic));
    }

    @Test
    void shouldResolveOrderedDeploymentIds() {
        registry.register(new DeploymentDependency("dep-1", "deploy-B", "deploy-A", DependencyType.REQUIRES_SUCCESS));
        registry.register(new DeploymentDependency("dep-2", "deploy-C", "deploy-B", DependencyType.REQUIRES_SUCCESS));
        List<String> order = registry.getOrderedDeploymentIds("deploy-C");
        int indexA = order.indexOf("deploy-A");
        int indexB = order.indexOf("deploy-B");
        int indexC = order.indexOf("deploy-C");
        assertTrue(indexA < indexB, "deploy-A should come before deploy-B");
        assertTrue(indexB < indexC, "deploy-B should come before deploy-C");
    }

    @Test
    void shouldClearAllDependencies() {
        registry.register(new DeploymentDependency("dep-1", "deploy-B", "deploy-A", DependencyType.REQUIRES_SUCCESS));
        registry.clear();
        assertEquals(0, registry.size());
    }
}
