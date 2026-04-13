package com.jarvis.deploy.pipeline;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single named stage within a DeploymentPipeline.
 */
public class PipelineStage {

    private final String name;
    private final int order;
    private PipelineStatus status;
    private String resultMessage;
    private Instant startedAt;
    private Instant finishedAt;

    public PipelineStage(String name, int order) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        if (order < 0) throw new IllegalArgumentException("order must be non-negative");
        this.order = order;
        this.status = PipelineStatus.PENDING;
    }

    public void markRunning() {
        this.status = PipelineStatus.RUNNING;
        this.startedAt = Instant.now();
    }

    public void markCompleted(String message) {
        this.status = PipelineStatus.COMPLETED;
        this.resultMessage = message;
        this.finishedAt = Instant.now();
    }

    public void markFailed(String message) {
        this.status = PipelineStatus.FAILED;
        this.resultMessage = message;
        this.finishedAt = Instant.now();
    }

    public void markSkipped(String reason) {
        this.status = PipelineStatus.SKIPPED;
        this.resultMessage = reason;
        this.finishedAt = Instant.now();
    }

    public String getName() { return name; }
    public int getOrder() { return order; }
    public PipelineStatus getStatus() { return status; }
    public String getResultMessage() { return resultMessage; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }

    @Override
    public String toString() {
        return "PipelineStage{name='" + name + "', order=" + order + ", status=" + status + "}";
    }
}
