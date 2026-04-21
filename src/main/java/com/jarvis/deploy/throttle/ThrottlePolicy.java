package com.jarvis.deploy.throttle;

import java.time.Duration;
import java.util.Objects;

/**
 * Defines throttling constraints for deployment operations per environment.
 */
public class ThrottlePolicy {

    private final String environment;
    private final int maxDeploymentsPerWindow;
    private final Duration windowDuration;
    private final boolean hardLimit;

    public ThrottlePolicy(String environment, int maxDeploymentsPerWindow, Duration windowDuration, boolean hardLimit) {
        if (maxDeploymentsPerWindow <= 0) {
            throw new IllegalArgumentException("maxDeploymentsPerWindow must be positive");
        }
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(windowDuration, "windowDuration must not be null");
        this.environment = environment;
        this.maxDeploymentsPerWindow = maxDeploymentsPerWindow;
        this.windowDuration = windowDuration;
        this.hardLimit = hardLimit;
    }

    public String getEnvironment() {
        return environment;
    }

    public int getMaxDeploymentsPerWindow() {
        return maxDeploymentsPerWindow;
    }

    public Duration getWindowDuration() {
        return windowDuration;
    }

    public boolean isHardLimit() {
        return hardLimit;
    }

    @Override
    public String toString() {
        return "ThrottlePolicy{environment='" + environment + "', max=" + maxDeploymentsPerWindow
                + ", window=" + windowDuration + ", hardLimit=" + hardLimit + "}";
    }
}
