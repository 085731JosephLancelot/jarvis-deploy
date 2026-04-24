package com.jarvis.deploy.archive;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an archived deployment record, capturing the state
 * of a deployment at the time of archival for long-term retention.
 */
public class DeploymentArchive {

    private final String archiveId;
    private final String deploymentId;
    private final String environment;
    private final String version;
    private final Instant deployedAt;
    private final Instant archivedAt;
    private final ArchiveReason reason;
    private final Map<String, String> metadata;

    public DeploymentArchive(String archiveId, String deploymentId, String environment,
                             String version, Instant deployedAt, ArchiveReason reason) {
        this.archiveId = Objects.requireNonNull(archiveId, "archiveId must not be null");
        this.deploymentId = Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.deployedAt = Objects.requireNonNull(deployedAt, "deployedAt must not be null");
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
        this.archivedAt = Instant.now();
        this.metadata = new HashMap<>();
    }

    public void addMetadata(String key, String value) {
        Objects.requireNonNull(key, "metadata key must not be null");
        metadata.put(key, value);
    }

    public String getArchiveId() { return archiveId; }
    public String getDeploymentId() { return deploymentId; }
    public String getEnvironment() { return environment; }
    public String getVersion() { return version; }
    public Instant getDeployedAt() { return deployedAt; }
    public Instant getArchivedAt() { return archivedAt; }
    public ArchiveReason getReason() { return reason; }
    public Map<String, String> getMetadata() { return Collections.unmodifiableMap(metadata); }

    @Override
    public String toString() {
        return String.format("DeploymentArchive{archiveId='%s', deploymentId='%s', env='%s', version='%s', reason=%s}",
                archiveId, deploymentId, environment, version, reason);
    }
}
