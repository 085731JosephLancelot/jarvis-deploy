package com.jarvis.deploy.correlation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationTrackerTest {

    private CorrelationTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new CorrelationTracker();
    }

    @Test
    void register_shouldCreateAndStoreCorrelation() {
        DeploymentCorrelation c = tracker.register("deploy-1", "staging");
        assertNotNull(c);
        assertEquals("deploy-1", c.getDeploymentId());
        assertEquals("staging", c.getEnvironment());
        assertNotNull(c.getCorrelationId());
        assertNotNull(c.getTraceId());
        assertFalse(c.hasParent());
        assertEquals(1, tracker.size());
    }

    @Test
    void registerChild_shouldLinkToParent() {
        DeploymentCorrelation parent = tracker.register("deploy-1", "staging");
        DeploymentCorrelation child = tracker.registerChild("deploy-2", "production", parent.getCorrelationId());

        assertTrue(child.hasParent());
        assertEquals(parent.getCorrelationId(), child.getParentCorrelationId());
        assertEquals(2, tracker.size());
    }

    @Test
    void registerChild_withUnknownParent_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> tracker.registerChild("deploy-x", "prod", "nonexistent-id"));
    }

    @Test
    void findById_shouldReturnCorrectCorrelation() {
        DeploymentCorrelation c = tracker.register("deploy-3", "dev");
        Optional<DeploymentCorrelation> found = tracker.findById(c.getCorrelationId());
        assertTrue(found.isPresent());
        assertEquals(c.getCorrelationId(), found.get().getCorrelationId());
    }

    @Test
    void findByDeploymentId_shouldReturnAllMatches() {
        tracker.register("deploy-A", "dev");
        tracker.register("deploy-A", "staging");
        tracker.register("deploy-B", "dev");

        List<DeploymentCorrelation> results = tracker.findByDeploymentId("deploy-A");
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(c -> c.getDeploymentId().equals("deploy-A")));
    }

    @Test
    void findByEnvironment_shouldReturnSortedByCreatedAtDesc() {
        tracker.register("deploy-1", "prod");
        tracker.register("deploy-2", "prod");
        tracker.register("deploy-3", "staging");

        List<DeploymentCorrelation> results = tracker.findByEnvironment("prod");
        assertEquals(2, results.size());
    }

    @Test
    void findChildren_shouldReturnDirectChildren() {
        DeploymentCorrelation parent = tracker.register("deploy-root", "dev");
        tracker.registerChild("deploy-child1", "staging", parent.getCorrelationId());
        tracker.registerChild("deploy-child2", "prod", parent.getCorrelationId());

        List<DeploymentCorrelation> children = tracker.findChildren(parent.getCorrelationId());
        assertEquals(2, children.size());
    }

    @Test
    void remove_shouldDeleteCorrelation() {
        DeploymentCorrelation c = tracker.register("deploy-rm", "dev");
        assertTrue(tracker.remove(c.getCorrelationId()));
        assertFalse(tracker.findById(c.getCorrelationId()).isPresent());
        assertEquals(0, tracker.size());
    }

    @Test
    void remove_withUnknownId_shouldReturnFalse() {
        assertFalse(tracker.remove("does-not-exist"));
    }
}
