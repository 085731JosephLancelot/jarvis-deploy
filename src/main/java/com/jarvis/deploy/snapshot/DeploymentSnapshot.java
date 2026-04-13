package com.jarvis.deploy.snapshot;

import com.jarvis.deploy.deployment.DeploymentStatus;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a point-in-time snapshot of a deployment's state,
 * used for rollback and audit trail purposes.
 */
public class DeploymentSnapshot {

    private final String snapshotId;
    private final String deploymentId;
    private final String environment;
    private final String version;
    private final DeploymentStatus status;
    private final Instant capturedAt;
    private final Map<String, String> metadata;

    public DeploymentSnapshot(String deploymentId, String environment,
                               String version, DeploymentStatus status,
                               Map<String, String> metadata) {
        this.snapshotId = UUID.randomUUID().toString();
        this.deploymentId = Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.capturedAt = Instant.now();
        this.metadata = metadata != null ? Collections.unmodifiableMap(new HashMap<>(metadata)) : Collections.emptyMap();
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getVersion() {
        return version;
    }

    public DeploymentStatus getStatus() {
        return status;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "DeploymentSnapshot{" +
                "snapshotId='" + snapshotId + '\'' +
                ", deploymentId='" + deploymentId + '\'' +
                ", environment='" + environment + '\'' +
                ", version='" + version + '\'' +
                ", status=" + status +
                ", capturedAt=" + capturedAt +
                '}';
    }
}
