package com.jarvis.deploy.lock;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a deployment lock for a specific environment,
 * preventing concurrent deployments from occurring.
 */
public class DeploymentLock {

    private final String environmentName;
    private final String acquiredBy;
    private final Instant acquiredAt;
    private final String deploymentId;

    public DeploymentLock(String environmentName, String acquiredBy, String deploymentId) {
        if (environmentName == null || environmentName.isBlank()) {
            throw new IllegalArgumentException("Environment name must not be blank");
        }
        if (acquiredBy == null || acquiredBy.isBlank()) {
            throw new IllegalArgumentException("acquiredBy must not be blank");
        }
        if (deploymentId == null || deploymentId.isBlank()) {
            throw new IllegalArgumentException("deploymentId must not be blank");
        }
        this.environmentName = environmentName;
        this.acquiredBy = acquiredBy;
        this.deploymentId = deploymentId;
        this.acquiredAt = Instant.now();
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public String getAcquiredBy() {
        return acquiredBy;
    }

    public Instant getAcquiredAt() {
        return acquiredAt;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentLock)) return false;
        DeploymentLock that = (DeploymentLock) o;
        return Objects.equals(environmentName, that.environmentName) &&
               Objects.equals(deploymentId, that.deploymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(environmentName, deploymentId);
    }

    @Override
    public String toString() {
        return "DeploymentLock{env='" + environmentName + "', acquiredBy='" + acquiredBy +
               "', deploymentId='" + deploymentId + "', acquiredAt=" + acquiredAt + "}";
    }
}
