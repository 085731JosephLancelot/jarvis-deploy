package com.jarvis.deploy.quota;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a deployment quota for a specific environment,
 * limiting how many deployments can occur within a time window.
 */
public class DeploymentQuota {

    private final String environment;
    private final int maxDeployments;
    private final long windowSeconds;
    private int usedDeployments;
    private Instant windowStart;

    public DeploymentQuota(String environment, int maxDeployments, long windowSeconds) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        if (maxDeployments <= 0) {
            throw new IllegalArgumentException("maxDeployments must be positive");
        }
        if (windowSeconds <= 0) {
            throw new IllegalArgumentException("windowSeconds must be positive");
        }
        this.environment = environment;
        this.maxDeployments = maxDeployments;
        this.windowSeconds = windowSeconds;
        this.usedDeployments = 0;
        this.windowStart = Instant.now();
    }

    public String getEnvironment() {
        return environment;
    }

    public int getMaxDeployments() {
        return maxDeployments;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }

    public int getUsedDeployments() {
        refreshWindowIfExpired();
        return usedDeployments;
    }

    public int getRemainingDeployments() {
        refreshWindowIfExpired();
        return maxDeployments - usedDeployments;
    }

    public boolean canDeploy() {
        refreshWindowIfExpired();
        return usedDeployments < maxDeployments;
    }

    public void recordDeployment() {
        refreshWindowIfExpired();
        if (!canDeploy()) {
            throw new QuotaExceededException(
                "Quota exceeded for environment '" + environment + "': " +
                usedDeployments + "/" + maxDeployments + " deployments used in current window"
            );
        }
        usedDeployments++;
    }

    private void refreshWindowIfExpired() {
        Instant now = Instant.now();
        if (now.isAfter(windowStart.plusSeconds(windowSeconds))) {
            windowStart = now;
            usedDeployments = 0;
        }
    }

    public Instant getWindowStart() {
        return windowStart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentQuota)) return false;
        DeploymentQuota that = (DeploymentQuota) o;
        return Objects.equals(environment, that.environment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(environment);
    }

    @Override
    public String toString() {
        return "DeploymentQuota{environment='" + environment + "', used=" + usedDeployments +
               "/" + maxDeployments + ", windowSeconds=" + windowSeconds + "}";
    }
}
