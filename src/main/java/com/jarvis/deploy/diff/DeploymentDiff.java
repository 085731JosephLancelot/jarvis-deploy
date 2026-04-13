package com.jarvis.deploy.diff;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a diff between two deployment configurations,
 * capturing added, removed, and changed key-value pairs.
 */
public class DeploymentDiff {

    private final String fromVersion;
    private final String toVersion;
    private final String environment;
    private final Instant generatedAt;
    private final Map<String, String> added;
    private final Map<String, String> removed;
    private final Map<String, DiffEntry> changed;

    public DeploymentDiff(String fromVersion, String toVersion, String environment,
                          Map<String, String> added,
                          Map<String, String> removed,
                          Map<String, DiffEntry> changed) {
        this.fromVersion = Objects.requireNonNull(fromVersion, "fromVersion must not be null");
        this.toVersion = Objects.requireNonNull(toVersion, "toVersion must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.generatedAt = Instant.now();
        this.added = Collections.unmodifiableMap(new HashMap<>(added));
        this.removed = Collections.unmodifiableMap(new HashMap<>(removed));
        this.changed = Collections.unmodifiableMap(new HashMap<>(changed));
    }

    public String getFromVersion() { return fromVersion; }
    public String getToVersion() { return toVersion; }
    public String getEnvironment() { return environment; }
    public Instant getGeneratedAt() { return generatedAt; }
    public Map<String, String> getAdded() { return added; }
    public Map<String, String> getRemoved() { return removed; }
    public Map<String, DiffEntry> getChanged() { return changed; }

    public boolean isEmpty() {
        return added.isEmpty() && removed.isEmpty() && changed.isEmpty();
    }

    public int totalChanges() {
        return added.size() + removed.size() + changed.size();
    }

    @Override
    public String toString() {
        return String.format("DeploymentDiff[%s -> %s, env=%s, changes=%d]",
                fromVersion, toVersion, environment, totalChanges());
    }

    /** Holds the before and after values for a changed key. */
    public static class DiffEntry {
        private final String before;
        private final String after;

        public DiffEntry(String before, String after) {
            this.before = before;
            this.after = after;
        }

        public String getBefore() { return before; }
        public String getAfter() { return after; }

        @Override
        public String toString() {
            return String.format("%s -> %s", before, after);
        }
    }
}
