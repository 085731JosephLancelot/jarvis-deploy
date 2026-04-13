package com.jarvis.deploy.changelog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ChangelogServiceTest {

    private ChangelogService service;

    @BeforeEach
    void setUp() {
        service = new ChangelogService();
    }

    @Test
    void shouldRecordAndReturnEntry() {
        ChangelogEntry entry = service.record("d1", "prod", "1.0.0", "alice", "first", ChangelogEntryType.DEPLOY);
        assertNotNull(entry);
        assertNotNull(entry.getId());
        assertEquals("prod", entry.getEnvironment());
        assertEquals(ChangelogEntryType.DEPLOY, entry.getType());
    }

    @Test
    void shouldReturnEntriesByEnvironment() {
        service.record("d1", "prod", "1.0.0", "alice", "", ChangelogEntryType.DEPLOY);
        service.record("d2", "staging", "1.0.0", "bob", "", ChangelogEntryType.DEPLOY);
        service.record("d3", "prod", "1.1.0", "alice", "", ChangelogEntryType.HOTFIX);

        List<ChangelogEntry> prodEntries = service.getByEnvironment("prod");
        assertEquals(2, prodEntries.size());
        assertTrue(prodEntries.stream().allMatch(e -> e.getEnvironment().equals("prod")));
    }

    @Test
    void shouldReturnEntriesByDeploymentId() {
        service.record("deploy-99", "prod", "2.0.0", "carol", "deploy", ChangelogEntryType.DEPLOY);
        service.record("deploy-99", "prod", "1.9.0", "carol", "rollback", ChangelogEntryType.ROLLBACK);
        service.record("deploy-77", "prod", "2.0.0", "dave", "other", ChangelogEntryType.DEPLOY);

        List<ChangelogEntry> result = service.getByDeploymentId("deploy-99");
        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnLatestEntryForEnvironment() {
        service.record("d1", "prod", "1.0.0", "alice", "", ChangelogEntryType.DEPLOY);
        service.record("d2", "prod", "1.1.0", "alice", "", ChangelogEntryType.HOTFIX);

        Optional<ChangelogEntry> latest = service.getLatestForEnvironment("prod");
        assertTrue(latest.isPresent());
        assertEquals("1.1.0", latest.get().getVersion());
    }

    @Test
    void shouldReturnEmptyOptionalWhenNoEntriesForEnvironment() {
        Optional<ChangelogEntry> latest = service.getLatestForEnvironment("unknown-env");
        assertFalse(latest.isPresent());
    }

    @Test
    void getAllShouldReturnAllEntriesNewestFirst() {
        service.record("d1", "prod", "1.0.0", "alice", "", ChangelogEntryType.DEPLOY);
        service.record("d2", "staging", "1.0.0", "bob", "", ChangelogEntryType.DEPLOY);

        List<ChangelogEntry> all = service.getAll();
        assertEquals(2, all.size());
        assertTrue(all.get(0).getTimestamp().compareTo(all.get(1).getTimestamp()) >= 0);
    }

    @Test
    void shouldThrowOnNullArguments() {
        assertThrows(NullPointerException.class, () ->
                service.record(null, "prod", "1.0", "alice", "", ChangelogEntryType.DEPLOY));
        assertThrows(NullPointerException.class, () ->
                service.getByEnvironment(null));
        assertThrows(NullPointerException.class, () ->
                service.getByDeploymentId(null));
    }

    @Test
    void clearShouldRemoveAllEntries() {
        service.record("d1", "prod", "1.0.0", "alice", "", ChangelogEntryType.DEPLOY);
        service.clear();
        assertTrue(service.getAll().isEmpty());
    }
}
