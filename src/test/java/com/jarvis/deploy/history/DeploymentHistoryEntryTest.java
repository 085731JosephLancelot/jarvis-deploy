package com.jarvis.deploy.history;

import com.jarvis.deploy.deployment.DeploymentStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentHistoryEntryTest {

    private DeploymentHistoryEntry buildEntry(DeploymentStatus status) {
        return new DeploymentHistoryEntry(
                "entry-1", "deploy-42", "production",
                "2.3.1", status, "alice", Instant.now(), "routine release");
    }

    @Test
    void shouldStoreAllFields() {
        DeploymentHistoryEntry entry = buildEntry(DeploymentStatus.SUCCESS);
        assertEquals("entry-1", entry.getEntryId());
        assertEquals("deploy-42", entry.getDeploymentId());
        assertEquals("production", entry.getEnvironment());
        assertEquals("2.3.1", entry.getVersion());
        assertEquals(DeploymentStatus.SUCCESS, entry.getStatus());
        assertEquals("alice", entry.getTriggeredBy());
        assertEquals("routine release", entry.getNotes());
        assertNotNull(entry.getTimestamp());
    }

    @Test
    void shouldAllowNullNotes() {
        DeploymentHistoryEntry entry = new DeploymentHistoryEntry(
                "e2", "d2", "staging", "1.0.0",
                DeploymentStatus.FAILED, "bob", Instant.now(), null);
        assertNull(entry.getNotes());
    }

    @Test
    void shouldRejectNullRequiredFields() {
        assertThrows(NullPointerException.class, () ->
                new DeploymentHistoryEntry(null, "d", "env", "v",
                        DeploymentStatus.SUCCESS, "user", Instant.now(), null));
        assertThrows(NullPointerException.class, () ->
                new DeploymentHistoryEntry("id", null, "env", "v",
                        DeploymentStatus.SUCCESS, "user", Instant.now(), null));
        assertThrows(NullPointerException.class, () ->
                new DeploymentHistoryEntry("id", "d", null, "v",
                        DeploymentStatus.SUCCESS, "user", Instant.now(), null));
    }

    @Test
    void toStringShouldContainKeyFields() {
        DeploymentHistoryEntry entry = buildEntry(DeploymentStatus.SUCCESS);
        String str = entry.toString();
        assertTrue(str.contains("entry-1"));
        assertTrue(str.contains("deploy-42"));
        assertTrue(str.contains("production"));
        assertTrue(str.contains("SUCCESS"));
    }
}
