package com.jarvis.deploy.baseline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BaselineManagerTest {

    private BaselineManager manager;
    private Map<String, String> sampleConfig;

    @BeforeEach
    void setUp() {
        manager = new BaselineManager();
        sampleConfig = new HashMap<>();
        sampleConfig.put("replicas", "3");
        sampleConfig.put("image", "app:1.0.0");
        sampleConfig.put("memory", "512Mi");
    }

    @Test
    void captureCreatesActiveBaseline() {
        DeploymentBaseline baseline = manager.capture("prod", "1.0.0", sampleConfig);
        assertNotNull(baseline.getId());
        assertEquals("prod", baseline.getEnvironmentId());
        assertEquals("1.0.0", baseline.getVersion());
        assertEquals(BaselineStatus.ACTIVE, baseline.getStatus());
    }

    @Test
    void capturingNewBaselineArchivesPrevious() {
        DeploymentBaseline first = manager.capture("prod", "1.0.0", sampleConfig);
        manager.capture("prod", "1.1.0", sampleConfig);
        assertEquals(BaselineStatus.ARCHIVED, first.getStatus());
    }

    @Test
    void getActiveBaselineReturnsLatest() {
        manager.capture("prod", "1.0.0", sampleConfig);
        manager.capture("prod", "1.1.0", sampleConfig);
        Optional<DeploymentBaseline> active = manager.getActiveBaseline("prod");
        assertTrue(active.isPresent());
        assertEquals("1.1.0", active.get().getVersion());
    }

    @Test
    void getActiveBaselineReturnsEmptyForUnknownEnvironment() {
        assertTrue(manager.getActiveBaseline("staging").isEmpty());
    }

    @Test
    void listByEnvironmentReturnsSortedDescending() {
        manager.capture("staging", "1.0.0", sampleConfig);
        manager.capture("staging", "1.1.0", sampleConfig);
        List<DeploymentBaseline> list = manager.listByEnvironment("staging");
        assertEquals(2, list.size());
        assertEquals("1.1.0", list.get(0).getVersion());
    }

    @Test
    void detectDriftReturnsDiffWhenConfigChanged() {
        manager.capture("prod", "1.0.0", sampleConfig);
        Map<String, String> drifted = new HashMap<>(sampleConfig);
        drifted.put("replicas", "5");
        Optional<Map<String, String>> drift = manager.detectDrift("prod", drifted);
        assertTrue(drift.isPresent());
        assertTrue(drift.get().containsKey("replicas"));
    }

    @Test
    void detectDriftReturnsEmptyMapWhenNoChange() {
        manager.capture("prod", "1.0.0", sampleConfig);
        Optional<Map<String, String>> drift = manager.detectDrift("prod", new HashMap<>(sampleConfig));
        assertTrue(drift.isPresent());
        assertTrue(drift.get().isEmpty());
    }

    @Test
    void archiveBaselineChangesStatus() {
        DeploymentBaseline baseline = manager.capture("dev", "1.0.0", sampleConfig);
        assertTrue(manager.archiveBaseline(baseline.getId()));
        assertEquals(BaselineStatus.ARCHIVED, baseline.getStatus());
    }

    @Test
    void archiveBaselineReturnsFalseForUnknownId() {
        assertFalse(manager.archiveBaseline("nonexistent-id"));
    }

    @Test
    void baselineIsolatedAcrossEnvironments() {
        manager.capture("prod", "1.0.0", sampleConfig);
        manager.capture("staging", "2.0.0", sampleConfig);
        assertEquals(1, manager.listByEnvironment("prod").size());
        assertEquals(1, manager.listByEnvironment("staging").size());
    }
}
