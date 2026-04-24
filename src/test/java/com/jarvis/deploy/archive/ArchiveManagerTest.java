package com.jarvis.deploy.archive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ArchiveManagerTest {

    private ArchiveManager archiveManager;

    @BeforeEach
    void setUp() {
        archiveManager = new ArchiveManager(3);
    }

    @Test
    void shouldArchiveDeploymentSuccessfully() {
        DeploymentArchive archive = archiveManager.archive(
                "dep-001", "production", "1.0.0", Instant.now(), ArchiveReason.SUPERSEDED);

        assertNotNull(archive);
        assertNotNull(archive.getArchiveId());
        assertEquals("dep-001", archive.getDeploymentId());
        assertEquals("production", archive.getEnvironment());
        assertEquals("1.0.0", archive.getVersion());
        assertEquals(ArchiveReason.SUPERSEDED, archive.getReason());
        assertNotNull(archive.getArchivedAt());
    }

    @Test
    void shouldFindArchiveById() {
        DeploymentArchive created = archiveManager.archive(
                "dep-002", "staging", "2.0.0", Instant.now(), ArchiveReason.ROLLED_BACK);

        Optional<DeploymentArchive> found = archiveManager.findById(created.getArchiveId());
        assertTrue(found.isPresent());
        assertEquals(created.getArchiveId(), found.get().getArchiveId());
    }

    @Test
    void shouldReturnEmptyForUnknownArchiveId() {
        Optional<DeploymentArchive> found = archiveManager.findById("non-existent");
        assertFalse(found.isPresent());
    }

    @Test
    void shouldFindArchivesByEnvironment() {
        archiveManager.archive("dep-010", "production", "1.0.0", Instant.now(), ArchiveReason.SUPERSEDED);
        archiveManager.archive("dep-011", "production", "1.1.0", Instant.now(), ArchiveReason.SUPERSEDED);
        archiveManager.archive("dep-012", "staging", "1.0.0", Instant.now(), ArchiveReason.EXPIRED);

        List<DeploymentArchive> prodArchives = archiveManager.findByEnvironment("production");
        assertEquals(2, prodArchives.size());

        List<DeploymentArchive> stagingArchives = archiveManager.findByEnvironment("staging");
        assertEquals(1, stagingArchives.size());
    }

    @Test
    void shouldFindArchivesByReason() {
        archiveManager.archive("dep-020", "production", "1.0.0", Instant.now(), ArchiveReason.ROLLED_BACK);
        archiveManager.archive("dep-021", "staging", "1.1.0", Instant.now(), ArchiveReason.ROLLED_BACK);
        archiveManager.archive("dep-022", "dev", "1.2.0", Instant.now(), ArchiveReason.EXPIRED);

        List<DeploymentArchive> rolledBack = archiveManager.findByReason(ArchiveReason.ROLLED_BACK);
        assertEquals(2, rolledBack.size());
    }

    @Test
    void shouldPruneOldestArchivesWhenLimitExceeded() {
        archiveManager.archive("dep-030", "production", "1.0.0", Instant.now(), ArchiveReason.SUPERSEDED);
        archiveManager.archive("dep-031", "production", "1.1.0", Instant.now(), ArchiveReason.SUPERSEDED);
        archiveManager.archive("dep-032", "production", "1.2.0", Instant.now(), ArchiveReason.SUPERSEDED);
        archiveManager.archive("dep-033", "production", "1.3.0", Instant.now(), ArchiveReason.SUPERSEDED);

        List<DeploymentArchive> archives = archiveManager.findByEnvironment("production");
        assertEquals(3, archives.size());
    }

    @Test
    void shouldRemoveArchiveById() {
        DeploymentArchive archive = archiveManager.archive(
                "dep-040", "dev", "0.1.0", Instant.now(), ArchiveReason.MANUAL);

        boolean removed = archiveManager.remove(archive.getArchiveId());
        assertTrue(removed);
        assertFalse(archiveManager.findById(archive.getArchiveId()).isPresent());
    }

    @Test
    void shouldRejectInvalidMaxArchives() {
        assertThrows(IllegalArgumentException.class, () -> new ArchiveManager(0));
        assertThrows(IllegalArgumentException.class, () -> new ArchiveManager(-1));
    }

    @Test
    void shouldSupportMetadataOnArchive() {
        DeploymentArchive archive = archiveManager.archive(
                "dep-050", "production", "3.0.0", Instant.now(), ArchiveReason.SUPERSEDED);
        archive.addMetadata("triggeredBy", "ci-pipeline");
        archive.addMetadata("buildNumber", "42");

        assertEquals("ci-pipeline", archive.getMetadata().get("triggeredBy"));
        assertEquals("42", archive.getMetadata().get("buildNumber"));
    }
}
