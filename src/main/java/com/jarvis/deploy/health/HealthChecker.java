package com.jarvis.deploy.health;

import com.jarvis.deploy.environment.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Performs health checks against deployment environments.
 * Verifies connectivity and readiness before and after deployments.
 */
public class HealthChecker {

    private static final Logger logger = Logger.getLogger(HealthChecker.class.getName());

    private final int timeoutSeconds;
    private final int maxRetries;

    public HealthChecker(int timeoutSeconds, int maxRetries) {
        if (timeoutSeconds <= 0) throw new IllegalArgumentException("Timeout must be positive");
        if (maxRetries < 0) throw new IllegalArgumentException("Max retries cannot be negative");
        this.timeoutSeconds = timeoutSeconds;
        this.maxRetries = maxRetries;
    }

    /**
     * Runs a health check for the given environment.
     *
     * @param environment the target environment
     * @return a HealthCheckResult describing the outcome
     */
    public HealthCheckResult check(Environment environment) {
        if (environment == null) {
            throw new IllegalArgumentException("Environment must not be null");
        }

        logger.info("Running health check for environment: " + environment.getName());

        Map<String, String> details = new HashMap<>();
        details.put("environment", environment.getName());
        details.put("region", environment.getRegion());
        details.put("timeoutSeconds", String.valueOf(timeoutSeconds));

        boolean healthy = performCheck(environment);

        String message = healthy
                ? "Environment " + environment.getName() + " is healthy"
                : "Environment " + environment.getName() + " failed health check after " + maxRetries + " retries";

        logger.info(message);
        return new HealthCheckResult(environment.getName(), healthy, message, details);
    }

    /**
     * Simulates the actual connectivity/readiness probe with retry logic.
     */
    private boolean performCheck(Environment environment) {
        for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {
            logger.fine("Health check attempt " + attempt + " for " + environment.getName());
            // In production this would perform an HTTP ping or socket check
            if (environment.isActive()) {
                return true;
            }
        }
        return false;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public int getMaxRetries() {
        return maxRetries;
    }
}
