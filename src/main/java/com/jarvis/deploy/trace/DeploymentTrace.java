package com.jarvis.deploy.trace;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Represents a distributed trace for a single deployment operation,
 * capturing ordered spans across pipeline stages and services.
 */
public class DeploymentTrace {

    private final String traceId;
    private final String deploymentId;
    private final String environment;
    private final Instant startedAt;
    private final List<TraceSpan> spans;
    private Instant completedAt;

    public DeploymentTrace(String deploymentId, String environment) {
        this.traceId = UUID.randomUUID().toString();
        this.deploymentId = deploymentId;
        this.environment = environment;
        this.startedAt = Instant.now();
        this.spans = new ArrayList<>();
    }

    public void addSpan(TraceSpan span) {
        if (span == null) throw new IllegalArgumentException("Span must not be null");
        spans.add(span);
    }

    public void complete() {
        this.completedAt = Instant.now();
    }

    public boolean isCompleted() {
        return completedAt != null;
    }

    public long durationMillis() {
        Instant end = completedAt != null ? completedAt : Instant.now();
        return end.toEpochMilli() - startedAt.toEpochMilli();
    }

    public String getTraceId() { return traceId; }
    public String getDeploymentId() { return deploymentId; }
    public String getEnvironment() { return environment; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public List<TraceSpan> getSpans() { return Collections.unmodifiableList(spans); }

    @Override
    public String toString() {
        return String.format("DeploymentTrace{traceId='%s', deploymentId='%s', env='%s', spans=%d, durationMs=%d}",
                traceId, deploymentId, environment, spans.size(), durationMillis());
    }
}
