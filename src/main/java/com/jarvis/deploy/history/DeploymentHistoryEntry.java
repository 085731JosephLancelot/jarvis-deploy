package com.jarvis.deploy.history;

import com.jarvis.deploy.deployment.DeploymentStatus;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single entry in the deployment history log.
 */
public class DeploymentHistoryEntry {

    private final String entryId;
    private final String deploymentId;
    private final String environment;
    private final String version;
    private final DeploymentStatus status;
    private final String triggeredBy;
    private final Instant timestamp;
    private final String notes;

    public DeploymentHistoryEntry(String entryId, String deploymentId, String environment,
                                   String version, DeploymentStatus status,
                                   String triggeredBy, Instant timestamp, String notes) {
        this.entryId = Objects.requireNonNull(entryId, "entryId must not be null");
        this.deploymentId = Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.triggeredBy = Objects.requireNonNull(triggeredBy, "triggeredBy must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.notes = notes;
    }

    public String getEntryId() { return entryId; }
    public String getDeploymentId() { return deploymentId; }
    public String getEnvironment() { return environment; }
    public String getVersion() { return version; }
    public DeploymentStatus getStatus() { return status; }
    public String getTriggeredBy() { return triggeredBy; }
    public Instant getTimestamp() { return timestamp; }
    public String getNotes() { return notes; }

    @Override
    public String toString() {
        return String.format("DeploymentHistoryEntry{entryId='%s', deploymentId='%s', env='%s', " +
                "version='%s', status=%s, triggeredBy='%s', timestamp=%s}",
                entryId, deploymentId, environment, version, status, triggeredBy, timestamp);
    }
}
