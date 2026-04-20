package com.jarvis.deploy.timeout;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a timeout configuration for a deployment operation.
 */
public class DeploymentTimeout {

    private final String deploymentId;
    private final Duration limit;
    private final Instant startedAt;
    private boolean cancelled;

    public DeploymentTimeout(String deploymentId, Duration limit) {
        this(deploymentId, limit, Instant.now());
    }

    public DeploymentTimeout(String deploymentId, Duration limit, Instant startedAt) {
        Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        Objects.requireNonNull(limit, "limit must not be null");
        Objects.requireNonNull(startedAt, "startedAt must not be null");
        if (limit.isNegative() || limit.isZero()) {
            throw new IllegalArgumentException("Timeout limit must be positive");
        }
        this.deploymentId = deploymentId;
        this.limit = limit;
        this.startedAt = startedAt;
        this.cancelled = false;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public Duration getLimit() {
        return limit;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isExpired() {
        if (cancelled) {
            return false;
        }
        return Duration.between(startedAt, Instant.now()).compareTo(limit) >= 0;
    }

    public Duration remaining() {
        Duration elapsed = Duration.between(startedAt, Instant.now());
        Duration remaining = limit.minus(elapsed);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    @Override
    public String toString() {
        return "DeploymentTimeout{deploymentId='" + deploymentId + "', limit=" + limit +
               ", startedAt=" + startedAt + ", cancelled=" + cancelled + "}";
    }
}
