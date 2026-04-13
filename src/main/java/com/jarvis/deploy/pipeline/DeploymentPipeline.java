package com.jarvis.deploy.pipeline;

import com.jarvis.deploy.deployment.Deployment;
import com.jarvis.deploy.deployment.DeploymentStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an ordered sequence of stages that a deployment must pass through.
 */
public class DeploymentPipeline {

    private final String pipelineId;
    private final String environment;
    private final List<PipelineStage> stages;
    private PipelineStatus status;
    private Instant startedAt;
    private Instant completedAt;
    private Deployment deployment;

    public DeploymentPipeline(String pipelineId, String environment) {
        this.pipelineId = Objects.requireNonNull(pipelineId, "pipelineId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.stages = new ArrayList<>();
        this.status = PipelineStatus.PENDING;
    }

    public void addStage(PipelineStage stage) {
        Objects.requireNonNull(stage, "stage must not be null");
        if (status != PipelineStatus.PENDING) {
            throw new IllegalStateException(
                "Cannot add stages to a pipeline that has already started (status=" + status + ")");
        }
        stages.add(stage);
    }

    public void start(Deployment deployment) {
        this.deployment = Objects.requireNonNull(deployment, "deployment must not be null");
        this.status = PipelineStatus.RUNNING;
        this.startedAt = Instant.now();
    }

    public void complete() {
        this.status = PipelineStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void fail() {
        this.status = PipelineStatus.FAILED;
        this.completedAt = Instant.now();
    }

    /**
     * Returns the current active stage (the first stage that is not yet completed or failed),
     * or an empty Optional if all stages have finished or no stages exist.
     */
    public java.util.Optional<PipelineStage> getActiveStage() {
        return stages.stream()
                .filter(s -> s.getStatus() != PipelineStageStatus.COMPLETED
                          && s.getStatus() != PipelineStageStatus.FAILED)
                .findFirst();
    }

    public String getPipelineId() { return pipelineId; }
    public String getEnvironment() { return environment; }
    public List<PipelineStage> getStages() { return Collections.unmodifiableList(stages); }
    public PipelineStatus getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Deployment getDeployment() { return deployment; }

    @Override
    public String toString() {
        return "DeploymentPipeline{pipelineId='" + pipelineId + "', environment='" + environment +
                "', status=" + status + ", stages=" + stages.size() + "}";
    }
}
