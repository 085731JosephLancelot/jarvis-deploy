package com.jarvis.deploy.checkpoint;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a named checkpoint in a deployment process that must be
 * evaluated before proceeding to the next stage.
 */
public class DeploymentCheckpoint {

    private final String id;
    private final String name;
    private final String deploymentId;
    private CheckpointStatus status;
    private String message;
    private final Instant createdAt;
    private Instant evaluatedAt;

    public DeploymentCheckpoint(String id, String name, String deploymentId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.deploymentId = Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        this.status = CheckpointStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void pass(String message) {
        this.status = CheckpointStatus.PASSED;
        this.message = message;
        this.evaluatedAt = Instant.now();
    }

    public void fail(String message) {
        this.status = CheckpointStatus.FAILED;
        this.message = message;
        this.evaluatedAt = Instant.now();
    }

    public void skip(String reason) {
        this.status = CheckpointStatus.SKIPPED;
        this.message = reason;
        this.evaluatedAt = Instant.now();
    }

    public boolean isPassed() {
        return status == CheckpointStatus.PASSED;
    }

    public boolean isFailed() {
        return status == CheckpointStatus.FAILED;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDeploymentId() { return deploymentId; }
    public CheckpointStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getEvaluatedAt() { return evaluatedAt; }

    @Override
    public String toString() {
        return "DeploymentCheckpoint{id='" + id + "', name='" + name +
               "', deploymentId='" + deploymentId + "', status=" + status + "}";
    }
}
