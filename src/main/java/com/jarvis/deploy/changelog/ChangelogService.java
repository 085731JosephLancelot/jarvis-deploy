package com.jarvis.deploy.changelog;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Manages the lifecycle of deployment changelog entries.
 * Entries are stored in-memory and can be queried by environment or deployment id.
 */
public class ChangelogService {

    private final List<ChangelogEntry> entries = new CopyOnWriteArrayList<>();

    /**
     * Records a new changelog entry and returns it.
     */
    public ChangelogEntry record(String deploymentId, String environment, String version,
                                  String author, String description, ChangelogEntryType type) {
        Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(version, "version must not be null");
        Objects.requireNonNull(author, "author must not be null");
        Objects.requireNonNull(type, "type must not be null");

        String id = UUID.randomUUID().toString();
        ChangelogEntry entry = new ChangelogEntry(id, deploymentId, environment,
                version, author, description, type, Instant.now());
        entries.add(entry);
        return entry;
    }

    /**
     * Returns all changelog entries for a given environment, ordered by timestamp ascending.
     */
    public List<ChangelogEntry> getByEnvironment(String environment) {
        Objects.requireNonNull(environment, "environment must not be null");
        return entries.stream()
                .filter(e -> e.getEnvironment().equals(environment))
                .sorted(Comparator.comparing(ChangelogEntry::getTimestamp))
                .collect(Collectors.toList());
    }

    /**
     * Returns all changelog entries for a given deployment id.
     */
    public List<ChangelogEntry> getByDeploymentId(String deploymentId) {
        Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        return entries.stream()
                .filter(e -> e.getDeploymentId().equals(deploymentId))
                .sorted(Comparator.comparing(ChangelogEntry::getTimestamp))
                .collect(Collectors.toList());
    }

    /**
     * Returns the most recent changelog entry for the given environment, or empty if none.
     */
    public Optional<ChangelogEntry> getLatestForEnvironment(String environment) {
        Objects.requireNonNull(environment, "environment must not be null");
        return entries.stream()
                .filter(e -> e.getEnvironment().equals(environment))
                .max(Comparator.comparing(ChangelogEntry::getTimestamp));
    }

    /**
     * Returns all entries regardless of environment, ordered by timestamp descending.
     */
    public List<ChangelogEntry> getAll() {
        return entries.stream()
                .sorted(Comparator.comparing(ChangelogEntry::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    /** Clears all entries — intended for testing purposes. */
    public void clear() {
        entries.clear();
    }
}
