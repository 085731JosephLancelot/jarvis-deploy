package com.jarvis.deploy.drain;

import java.time.Duration;
import java.util.Objects;

/**
 * Defines the policy for draining active connections/requests
 * before a deployment or rollback takes effect.
 */
public class DrainPolicy {

    private final String environmentName;
    private final Duration timeout;
    private final int maxRetries;
    private final boolean forceOnTimeout;

    public DrainPolicy(String environmentName, Duration timeout, int maxRetries, boolean forceOnTimeout) {
        if (environmentName == null || environmentName.isBlank()) {
            throw new IllegalArgumentException("Environment name must not be blank");
        }
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("Timeout must be a positive duration");
        }
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be non-negative");
        }
        this.environmentName = environmentName;
        this.timeout = timeout;
        this.maxRetries = maxRetries;
        this.forceOnTimeout = forceOnTimeout;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public boolean isForceOnTimeout() {
        return forceOnTimeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DrainPolicy)) return false;
        DrainPolicy that = (DrainPolicy) o;
        return maxRetries == that.maxRetries
                && forceOnTimeout == that.forceOnTimeout
                && Objects.equals(environmentName, that.environmentName)
                && Objects.equals(timeout, that.timeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(environmentName, timeout, maxRetries, forceOnTimeout);
    }

    @Override
    public String toString() {
        return "DrainPolicy{env='" + environmentName + "', timeout=" + timeout
                + ", maxRetries=" + maxRetries + ", forceOnTimeout=" + forceOnTimeout + '}';
    }
}
