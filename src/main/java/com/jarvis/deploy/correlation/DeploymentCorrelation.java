package com.jarvis.deploy.correlation;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a correlation context linking related deployments across environments
 * or pipeline stages for tracing and audit purposes.
 */
public class DeploymentCorrelation {

    private final String correlationId;
    private final String traceId;
    private final String deploymentId;
    private final String environment;
    private final Instant createdAt;
    private String parentCorrelationId;

    public DeploymentCorrelation(String deploymentId, String environment) {
        Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        this.correlationId = UUID.randomUUID().toString();
        this.traceId = UUID.randomUUID().toString();
        this.deploymentId = deploymentId;
        this.environment = environment;
        this.createdAt = Instant.now();
    }

    public DeploymentCorrelation(String deploymentId, String environment, String parentCorrelationId) {
        this(deploymentId, environment);
        this.parentCorrelationId = parentCorrelationId;
    }

    public String getCorrelationId() { return correlationId; }
    public String getTraceId() { return traceId; }
    public String getDeploymentId() { return deploymentId; }
    public String getEnvironment() { return environment; }
    public Instant getCreatedAt() { return createdAt; }
    public String getParentCorrelationId() { return parentCorrelationId; }

    public boolean hasParent() {
        return parentCorrelationId != null && !parentCorrelationId.isBlank();
    }

    @Override
    public String toString() {
        return String.format("DeploymentCorrelation{correlationId='%s', traceId='%s', deploymentId='%s', environment='%s', parent='%s'}",
                correlationId, traceId, deploymentId, environment, parentCorrelationId);
    }
}
