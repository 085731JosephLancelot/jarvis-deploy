package com.jarvis.deploy.changelog;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single entry in the deployment changelog.
 */
public class ChangelogEntry {

    private final String id;
    private final String deploymentId;
    private final String environment;
    private final String version;
    private final String author;
    private final String description;
    private final ChangelogEntryType type;
    private final Instant timestamp;

    public ChangelogEntry(String id, String deploymentId, String environment,
                          String version, String author, String description,
                          ChangelogEntryType type, Instant timestamp) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.deploymentId = Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.author = Objects.requireNonNull(author, "author must not be null");
        this.description = description != null ? description : "";
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    public String getId() { return id; }
    public String getDeploymentId() { return deploymentId; }
    public String getEnvironment() { return environment; }
    public String getVersion() { return version; }
    public String getAuthor() { return author; }
    public String getDescription() { return description; }
    public ChangelogEntryType getType() { return type; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s | env=%s ver=%s by=%s (%s)",
                timestamp, type, environment, version, author, description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChangelogEntry)) return false;
        ChangelogEntry that = (ChangelogEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
