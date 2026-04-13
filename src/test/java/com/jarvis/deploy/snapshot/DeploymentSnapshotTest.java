package com.jarvis.deploy.snapshot;

import com.jarvis.deploy.deployment.DeploymentStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentSnapshotTest {

    @Test
    void shouldCreateSnapshotWithGeneratedId() {
        DeploymentSnapshot snapshot = new DeploymentSnapshot(
                "dep-001", "production", "v1.2.3",
                DeploymentStatus.SUCCESS, null);

        assertNotNull(snapshot.getSnapshotId());
        assertFalse(snapshot.getSnapshotId().isBlank());
    }

    @Test
    void shouldRetainAllProvidedFields() {
        Map<String, String> meta = new HashMap<>();
        meta.put("triggeredBy", "alice");
        meta.put("buildNumber", "42");

        DeploymentSnapshot snapshot = new DeploymentSnapshot(
                "dep-002", "staging", "v2.0.0",
                DeploymentStatus.PENDING, meta);

        assertEquals("dep-002", snapshot.getDeploymentId());
        assertEquals("staging", snapshot.getEnvironment());
        assertEquals("v2.0.0", snapshot.getVersion());
        assertEquals(DeploymentStatus.PENDING, snapshot.getStatus());
        assertEquals("alice", snapshot.getMetadata().get("triggeredBy"));
        assertEquals("42", snapshot.getMetadata().get("buildNumber"));
    }

    @Test
    void shouldCaptureTimestampAtCreation() {
        Instant before = Instant.now();
        DeploymentSnapshot snapshot = new DeploymentSnapshot(
                "dep-003", "dev", "v0.9.0",
                DeploymentStatus.FAILED, null);
        Instant after = Instant.now();

        assertNotNull(snapshot.getCapturedAt());
        assertFalse(snapshot.getCapturedAt().isBefore(before));
        assertFalse(snapshot.getCapturedAt().isAfter(after));
    }

    @Test
    void shouldReturnEmptyMetadataWhenNullProvided() {
        DeploymentSnapshot snapshot = new DeploymentSnapshot(
                "dep-004", "qa", "v1.0.0",
                DeploymentStatus.SUCCESS, null);

        assertNotNull(snapshot.getMetadata());
        assertTrue(snapshot.getMetadata().isEmpty());
    }

    @Test
    void shouldReturnImmutableMetadata() {
        Map<String, String> meta = new HashMap<>();
        meta.put("key", "value");

        DeploymentSnapshot snapshot = new DeploymentSnapshot(
                "dep-005", "production", "v3.1.0",
                DeploymentStatus.SUCCESS, meta);

        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.getMetadata().put("newKey", "newValue"));
    }

    @Test
    void shouldThrowOnNullRequiredFields() {
        assertThrows(NullPointerException.class, () ->
                new DeploymentSnapshot(null, "production", "v1.0.0", DeploymentStatus.SUCCESS, null));
        assertThrows(NullPointerException.class, () ->
                new DeploymentSnapshot("dep-006", null, "v1.0.0", DeploymentStatus.SUCCESS, null));
        assertThrows(NullPointerException.class, () ->
                new DeploymentSnapshot("dep-007", "production", null, DeploymentStatus.SUCCESS, null));
        assertThrows(NullPointerException.class, () ->
                new DeploymentSnapshot("dep-008", "production", "v1.0.0", null, null));
    }

    @Test
    void shouldHaveUniqueSnapshotIds() {
        DeploymentSnapshot s1 = new DeploymentSnapshot("dep-009", "dev", "v1.0.0", DeploymentStatus.SUCCESS, null);
        DeploymentSnapshot s2 = new DeploymentSnapshot("dep-009", "dev", "v1.0.0", DeploymentStatus.SUCCESS, null);

        assertNotEquals(s1.getSnapshotId(), s2.getSnapshotId());
    }
}
