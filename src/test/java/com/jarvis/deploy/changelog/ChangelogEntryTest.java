package com.jarvis.deploy.changelog;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ChangelogEntryTest {

    private ChangelogEntry buildEntry() {
        return new ChangelogEntry(
                "entry-1", "deploy-42", "production",
                "2.3.1", "alice", "Initial release",
                ChangelogEntryType.DEPLOY, Instant.parse("2024-06-01T10:00:00Z")
        );
    }

    @Test
    void shouldStoreAllFields() {
        ChangelogEntry entry = buildEntry();
        assertEquals("entry-1", entry.getId());
        assertEquals("deploy-42", entry.getDeploymentId());
        assertEquals("production", entry.getEnvironment());
        assertEquals("2.3.1", entry.getVersion());
        assertEquals("alice", entry.getAuthor());
        assertEquals("Initial release", entry.getDescription());
        assertEquals(ChangelogEntryType.DEPLOY, entry.getType());
        assertNotNull(entry.getTimestamp());
    }

    @Test
    void shouldDefaultDescriptionToEmptyStringWhenNull() {
        ChangelogEntry entry = new ChangelogEntry(
                "e2", "d2", "staging", "1.0.0", "bob",
                null, ChangelogEntryType.ROLLBACK, Instant.now());
        assertEquals("", entry.getDescription());
    }

    @Test
    void shouldThrowOnNullRequiredFields() {
        assertThrows(NullPointerException.class, () ->
                new ChangelogEntry(null, "d", "e", "v", "a", "desc",
                        ChangelogEntryType.DEPLOY, Instant.now()));
        assertThrows(NullPointerException.class, () ->
                new ChangelogEntry("id", null, "e", "v", "a", "desc",
                        ChangelogEntryType.DEPLOY, Instant.now()));
    }

    @Test
    void equalsShouldBeBasedOnId() {
        Instant now = Instant.now();
        ChangelogEntry a = new ChangelogEntry("same", "d1", "prod", "1.0", "u", "", ChangelogEntryType.DEPLOY, now);
        ChangelogEntry b = new ChangelogEntry("same", "d2", "stg", "2.0", "v", "", ChangelogEntryType.ROLLBACK, now);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toStringShouldContainKeyFields() {
        ChangelogEntry entry = buildEntry();
        String str = entry.toString();
        assertTrue(str.contains("DEPLOY"));
        assertTrue(str.contains("production"));
        assertTrue(str.contains("2.3.1"));
        assertTrue(str.contains("alice"));
    }
}
