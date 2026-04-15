package com.jarvis.deploy.history;

import com.jarvis.deploy.deployment.DeploymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentHistoryServiceTest {

    private DeploymentHistoryService service;

    @BeforeEach
    void setUp() {
        service = new DeploymentHistoryService();
    }

    @Test
    void shouldRecordEntryAndReturnIt() {
        DeploymentHistoryEntry entry = service.record(
                "d1", "production", "1.0.0", DeploymentStatus.SUCCESS, "alice", "initial deploy");
        assertNotNull(entry);
        assertNotNull(entry.getEntryId());
        assertEquals("d1", entry.getDeploymentId());
        assertEquals(DeploymentStatus.SUCCESS, entry.getStatus());
    }

    @Test
    void shouldReturnAllEntries() {
        service.record("d1", "production", "1.0.0", DeploymentStatus.SUCCESS, "alice", null);
        service.record("d2", "staging", "1.1.0", DeploymentStatus.FAILED, "bob", null);
        assertEquals(2, service.getTotalCount());
        assertEquals(2, service.getAll().size());
    }

    @Test
    void shouldFilterByEnvironment() {
        service.record("d1", "production", "1.0.0", DeploymentStatus.SUCCESS, "alice", null);
        service.record("d2", "staging", "1.1.0", DeploymentStatus.SUCCESS, "bob", null);
        service.record("d3", "production", "1.2.0", DeploymentStatus.FAILED, "alice", null);

        List<DeploymentHistoryEntry> prodEntries = service.getByEnvironment("production");
        assertEquals(2, prodEntries.size());
        assertTrue(prodEntries.stream().allMatch(e -> e.getEnvironment().equals("production")));
    }

    @Test
    void shouldFilterByDeploymentId() {
        service.record("d1", "production", "1.0.0", DeploymentStatus.SUCCESS, "alice", null);
        service.record("d1", "staging", "1.0.0", DeploymentStatus.SUCCESS, "alice", null);
        service.record("d2", "production", "2.0.0", DeploymentStatus.SUCCESS, "bob", null);

        List<DeploymentHistoryEntry> d1Entries = service.getByDeploymentId("d1");
        assertEquals(2, d1Entries.size());
    }

    @Test
    void shouldFilterByStatus() {
        service.record("d1", "production", "1.0.0", DeploymentStatus.SUCCESS, "alice", null);
        service.record("d2", "staging", "1.1.0", DeploymentStatus.FAILED, "bob", null);
        service.record("d3", "production", "1.2.0", DeploymentStatus.FAILED, "carol", null);

        List<DeploymentHistoryEntry> failed = service.getByStatus(DeploymentStatus.FAILED);
        assertEquals(2, failed.size());
    }

    @Test
    void shouldReturnLatestForEnvironment() throws InterruptedException {
        service.record("d1", "production", "1.0.0", DeploymentStatus.SUCCESS, "alice", null);
        Thread.sleep(10);
        service.record("d2", "production", "2.0.0", DeploymentStatus.SUCCESS, "bob", null);

        Optional<DeploymentHistoryEntry> latest = service.getLatestForEnvironment("production");
        assertTrue(latest.isPresent());
        assertEquals("2.0.0", latest.get().getVersion());
    }

    @Test
    void shouldReturnEmptyOptionalForUnknownEnvironment() {
        Optional<DeploymentHistoryEntry> latest = service.getLatestForEnvironment("unknown");
        assertFalse(latest.isPresent());
    }

    @Test
    void shouldClearAllEntries() {
        service.record("d1", "production", "1.0.0", DeploymentStatus.SUCCESS, "alice", null);
        service.clear();
        assertEquals(0, service.getTotalCount());
    }

    @Test
    void shouldRejectNullEnvironmentFilter() {
        assertThrows(NullPointerException.class, () -> service.getByEnvironment(null));
    }
}
