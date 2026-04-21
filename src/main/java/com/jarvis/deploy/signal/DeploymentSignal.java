package com.jarvis.deploy.signal;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a signal emitted during a deployment lifecycle,
 * used to coordinate or interrupt active deployment processes.
 */
public class DeploymentSignal {

    private final String deploymentId;
    private final SignalType type;
    private final String issuedBy;
    private final String reason;
    private final Instant issuedAt;
    private boolean acknowledged;

    public DeploymentSignal(String deploymentId, SignalType type, String issuedBy, String reason) {
        Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(issuedBy, "issuedBy must not be null");
        this.deploymentId = deploymentId;
        this.type = type;
        this.issuedBy = issuedBy;
        this.reason = reason == null ? "" : reason;
        this.issuedAt = Instant.now();
        this.acknowledged = false;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public SignalType getType() {
        return type;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public String getReason() {
        return reason;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void acknowledge() {
        this.acknowledged = true;
    }

    @Override
    public String toString() {
        return "DeploymentSignal{deploymentId='" + deploymentId + "', type=" + type +
                ", issuedBy='" + issuedBy + "', reason='" + reason +
                "', issuedAt=" + issuedAt + ", acknowledged=" + acknowledged + "}";
    }
}
