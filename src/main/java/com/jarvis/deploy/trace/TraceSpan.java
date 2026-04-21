package com.jarvis.deploy.trace;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a single timed unit of work within a {@link DeploymentTrace}.
 */
public class TraceSpan {

    private final String spanId;
    private final String parentTraceId;
    private final String operationName;
    private final Instant startedAt;
    private final Map<String, String> tags;
    private Instant finishedAt;
    private boolean error;
    private String errorMessage;

    public TraceSpan(String parentTraceId, String operationName) {
        if (parentTraceId == null || parentTraceId.isBlank())
            throw new IllegalArgumentException("parentTraceId must not be blank");
        if (operationName == null || operationName.isBlank())
            throw new IllegalArgumentException("operationName must not be blank");
        this.spanId = UUID.randomUUID().toString();
        this.parentTraceId = parentTraceId;
        this.operationName = operationName;
        this.startedAt = Instant.now();
        this.tags = new HashMap<>();
    }

    public void tag(String key, String value) {
        tags.put(key, value);
    }

    public void finish() {
        this.finishedAt = Instant.now();
    }

    public void finishWithError(String message) {
        this.finishedAt = Instant.now();
        this.error = true;
        this.errorMessage = message;
    }

    public long durationMillis() {
        Instant end = finishedAt != null ? finishedAt : Instant.now();
        return end.toEpochMilli() - startedAt.toEpochMilli();
    }

    public String getSpanId() { return spanId; }
    public String getParentTraceId() { return parentTraceId; }
    public String getOperationName() { return operationName; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public boolean isError() { return error; }
    public String getErrorMessage() { return errorMessage; }
    public Map<String, String> getTags() { return Collections.unmodifiableMap(tags); }

    @Override
    public String toString() {
        return String.format("TraceSpan{spanId='%s', op='%s', durationMs=%d, error=%b}",
                spanId, operationName, durationMillis(), error);
    }
}
