package com.jarvis.deploy.retry;

import java.time.Duration;

/**
 * Defines retry behavior for deployment operations.
 */
public class RetryPolicy {

    private final int maxAttempts;
    private final Duration initialDelay;
    private final double backoffMultiplier;
    private final Duration maxDelay;

    private RetryPolicy(Builder builder) {
        this.maxAttempts = builder.maxAttempts;
        this.initialDelay = builder.initialDelay;
        this.backoffMultiplier = builder.backoffMultiplier;
        this.maxDelay = builder.maxDelay;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Duration getInitialDelay() {
        return initialDelay;
    }

    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    public Duration getMaxDelay() {
        return maxDelay;
    }

    /**
     * Computes the delay before the given attempt number using exponential backoff.
     * Attempt numbers are 1-based; attempt 1 uses the initial delay.
     *
     * @param attemptNumber the 1-based attempt number
     * @return the capped delay duration for that attempt
     */
    public Duration computeDelay(int attemptNumber) {
        if (attemptNumber <= 0) {
            return initialDelay;
        }
        long millis = (long) (initialDelay.toMillis() * Math.pow(backoffMultiplier, attemptNumber - 1));
        return Duration.ofMillis(Math.min(millis, maxDelay.toMillis()));
    }

    /**
     * Returns whether retrying is allowed after the given 1-based attempt number.
     *
     * @param attemptNumber the number of attempts already made
     * @return true if another attempt is permitted
     */
    public boolean shouldRetry(int attemptNumber) {
        return attemptNumber < maxAttempts;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static RetryPolicy noRetry() {
        return builder().maxAttempts(1).build();
    }

    public static RetryPolicy defaultPolicy() {
        return builder()
                .maxAttempts(3)
                .initialDelay(Duration.ofSeconds(2))
                .backoffMultiplier(2.0)
                .maxDelay(Duration.ofSeconds(30))
                .build();
    }

    public static class Builder {
        private int maxAttempts = 3;
        private Duration initialDelay = Duration.ofSeconds(1);
        private double backoffMultiplier = 2.0;
        private Duration maxDelay = Duration.ofSeconds(60);

        public Builder maxAttempts(int maxAttempts) {
            if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder initialDelay(Duration initialDelay) {
            this.initialDelay = initialDelay;
            return this;
        }

        public Builder backoffMultiplier(double backoffMultiplier) {
            if (backoffMultiplier < 1.0) throw new IllegalArgumentException("backoffMultiplier must be >= 1.0");
            this.backoffMultiplier = backoffMultiplier;
            return this;
        }

        public Builder maxDelay(Duration maxDelay) {
            this.maxDelay = maxDelay;
            return this;
        }

        public RetryPolicy build() {
            return new RetryPolicy(this);
        }
    }
}
