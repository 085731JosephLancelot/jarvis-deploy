package com.jarvis.deploy.diff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DiffServiceTest {

    private DiffService diffService;

    @BeforeEach
    void setUp() {
        diffService = new DiffService();
    }

    @Test
    void compute_noChanges_returnsEmptyDiff() {
        Map<String, String> config = Map.of("image", "app:1.0", "replicas", "3");
        DeploymentDiff diff = diffService.compute("v1", "v1", "staging", config, config);
        assertTrue(diff.isEmpty());
        assertEquals(0, diff.totalChanges());
    }

    @Test
    void compute_addedKeys_detectedCorrectly() {
        Map<String, String> from = Map.of("image", "app:1.0");
        Map<String, String> to = Map.of("image", "app:1.0", "replicas", "5");
        DeploymentDiff diff = diffService.compute("v1", "v2", "prod", from, to);
        assertEquals(1, diff.getAdded().size());
        assertEquals("5", diff.getAdded().get("replicas"));
        assertTrue(diff.getRemoved().isEmpty());
        assertTrue(diff.getChanged().isEmpty());
    }

    @Test
    void compute_removedKeys_detectedCorrectly() {
        Map<String, String> from = Map.of("image", "app:1.0", "debug", "true");
        Map<String, String> to = Map.of("image", "app:1.0");
        DeploymentDiff diff = diffService.compute("v1", "v2", "prod", from, to);
        assertEquals(1, diff.getRemoved().size());
        assertEquals("true", diff.getRemoved().get("debug"));
        assertTrue(diff.getAdded().isEmpty());
        assertTrue(diff.getChanged().isEmpty());
    }

    @Test
    void compute_changedValues_detectedCorrectly() {
        Map<String, String> from = Map.of("image", "app:1.0", "replicas", "2");
        Map<String, String> to = Map.of("image", "app:2.0", "replicas", "4");
        DeploymentDiff diff = diffService.compute("v1", "v2", "staging", from, to);
        assertEquals(2, diff.getChanged().size());
        assertEquals("app:1.0", diff.getChanged().get("image").getBefore());
        assertEquals("app:2.0", diff.getChanged().get("image").getAfter());
        assertTrue(diff.getAdded().isEmpty());
        assertTrue(diff.getRemoved().isEmpty());
    }

    @Test
    void compute_mixedChanges_totalChangesCorrect() {
        Map<String, String> from = Map.of("image", "app:1.0", "old-key", "val");
        Map<String, String> to = Map.of("image", "app:2.0", "new-key", "nval");
        DeploymentDiff diff = diffService.compute("v1", "v2", "dev", from, to);
        assertEquals(3, diff.totalChanges());
        assertFalse(diff.isEmpty());
    }

    @Test
    void format_emptyDiff_returnsNoChangesMessage() {
        Map<String, String> config = Map.of("k", "v");
        DeploymentDiff diff = diffService.compute("v1", "v1", "qa", config, config);
        String output = diffService.format(diff);
        assertTrue(output.contains("No changes"));
    }

    @Test
    void format_withChanges_containsSymbols() {
        Map<String, String> from = Map.of("image", "app:1.0", "old", "x");
        Map<String, String> to = Map.of("image", "app:2.0", "new", "y");
        DeploymentDiff diff = diffService.compute("v1", "v2", "prod", from, to);
        String output = diffService.format(diff);
        assertTrue(output.contains("+"));
        assertTrue(output.contains("-"));
        assertTrue(output.contains("~"));
    }

    @Test
    void compute_nullFromConfig_throwsException() {
        assertThrows(NullPointerException.class,
                () -> diffService.compute("v1", "v2", "prod", null, Map.of()));
    }
}
