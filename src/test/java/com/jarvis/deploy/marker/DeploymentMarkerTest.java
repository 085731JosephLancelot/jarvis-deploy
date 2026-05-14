package com.jarvis.deploy.marker;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentMarkerTest {

    @Test
    void shouldCreateMarkerWithRequiredFields() {
        DeploymentMarker marker = new DeploymentMarker("dep-001", "smoke-tests-passed");

        assertEquals("dep-001", marker.getDeploymentId());
        assertEquals("smoke-tests-passed", marker.getName());
        assertNotNull(marker.getCreatedAt());
        assertTrue(marker.getMetadata().isEmpty());
    }

    @Test
    void shouldCreateMarkerWithMetadata() {
        Map<String, String> meta = Map.of("triggered-by", "ci-pipeline", "build", "42");
        DeploymentMarker marker = new DeploymentMarker("dep-002", "canary-promoted", meta);

        assertEquals("ci-pipeline", marker.getMetadataValue("triggered-by"));
        assertEquals("42", marker.getMetadataValue("build"));
        assertEquals(2, marker.getMetadata().size());
    }

    @Test
    void shouldReturnNullForMissingMetadataKey() {
        DeploymentMarker marker = new DeploymentMarker("dep-003", "rollback-ready");
        assertNull(marker.getMetadataValue("nonexistent"));
    }

    @Test
    void shouldRejectBlankDeploymentId() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeploymentMarker("  ", "my-marker"));
    }

    @Test
    void shouldRejectNullDeploymentId() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeploymentMarker(null, "my-marker"));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeploymentMarker("dep-004", ""));
    }

    @Test
    void shouldEnforceMetadataImmutability() {
        Map<String, String> meta = new java.util.HashMap<>();
        meta.put("key", "value");
        DeploymentMarker marker = new DeploymentMarker("dep-005", "test-marker", meta);

        assertThrows(UnsupportedOperationException.class,
                () -> marker.getMetadata().put("extra", "data"));
    }

    @Test
    void shouldBeEqualWhenSameDeploymentIdAndName() {
        DeploymentMarker m1 = new DeploymentMarker("dep-006", "ready", Map.of("a", "1"));
        DeploymentMarker m2 = new DeploymentMarker("dep-006", "ready", Map.of("b", "2"));

        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentName() {
        DeploymentMarker m1 = new DeploymentMarker("dep-007", "alpha");
        DeploymentMarker m2 = new DeploymentMarker("dep-007", "beta");

        assertNotEquals(m1, m2);
    }

    @Test
    void createdAtShouldBeCloseToNow() {
        Instant before = Instant.now();
        DeploymentMarker marker = new DeploymentMarker("dep-008", "timing-check");
        Instant after = Instant.now();

        assertFalse(marker.getCreatedAt().isBefore(before));
        assertFalse(marker.getCreatedAt().isAfter(after));
    }

    @Test
    void toStringShouldContainKeyFields() {
        DeploymentMarker marker = new DeploymentMarker("dep-009", "info-marker");
        String str = marker.toString();

        assertTrue(str.contains("dep-009"));
        assertTrue(str.contains("info-marker"));
    }
}
