package com.jarvis.deploy.concurrency;

import java.util.Objects;

/**
 * Defines the concurrency policy for deployments in a given environment.
 * Controls how many simultaneous deployments are allowed and what strategy
 * to apply when the limit is exceeded.
 */
public class ConcurrencyPolicy {

    public enum OverflowStrategy {
        REJECT,
        QUEUE,
        CANCEL_OLDEST
    }

    private final String environment;
    private final int maxConcurrent;
    private final OverflowStrategy overflowStrategy;
    private final int queueCapacity;

    public ConcurrencyPolicy(String environment, int maxConcurrent,
                              OverflowStrategy overflowStrategy, int queueCapacity) {
        if (maxConcurrent < 1) {
            throw new IllegalArgumentException("maxConcurrent must be at least 1");
        }
        if (queueCapacity < 0) {
            throw new IllegalArgumentException("queueCapacity must be non-negative");
        }
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.maxConcurrent = maxConcurrent;
        this.overflowStrategy = Objects.requireNonNull(overflowStrategy, "overflowStrategy must not be null");
        this.queueCapacity = queueCapacity;
    }

    public static ConcurrencyPolicy strict(String environment) {
        return new ConcurrencyPolicy(environment, 1, OverflowStrategy.REJECT, 0);
    }

    public static ConcurrencyPolicy queued(String environment, int maxConcurrent, int queueCapacity) {
        return new ConcurrencyPolicy(environment, maxConcurrent, OverflowStrategy.QUEUE, queueCapacity);
    }

    public String getEnvironment() {
        return environment;
    }

    public int getMaxConcurrent() {
        return maxConcurrent;
    }

    public OverflowStrategy getOverflowStrategy() {
        return overflowStrategy;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    @Override
    public String toString() {
        return "ConcurrencyPolicy{env='" + environment + "', max=" + maxConcurrent
                + ", strategy=" + overflowStrategy + ", queueCap=" + queueCapacity + "}";
    }
}
