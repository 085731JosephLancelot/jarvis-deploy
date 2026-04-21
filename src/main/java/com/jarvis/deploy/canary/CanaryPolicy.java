package com.jarvis.deploy.canary;

import java.time.Duration;
import java.util.Objects;

/**
 * Defines the traffic-splitting and promotion rules for a canary deployment.
 */
public class CanaryPolicy {

    private final String name;
    private final int initialTrafficPercent;
    private final int targetTrafficPercent;
    private final int stepPercent;
    private final Duration stepInterval;
    private final double errorRateThreshold;

    private CanaryPolicy(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "name must not be null");
        if (builder.initialTrafficPercent < 0 || builder.initialTrafficPercent > 100) {
            throw new IllegalArgumentException("initialTrafficPercent must be between 0 and 100");
        }
        if (builder.targetTrafficPercent < 0 || builder.targetTrafficPercent > 100) {
            throw new IllegalArgumentException("targetTrafficPercent must be between 0 and 100");
        }
        if (builder.stepPercent <= 0) {
            throw new IllegalArgumentException("stepPercent must be positive");
        }
        this.initialTrafficPercent = builder.initialTrafficPercent;
        this.targetTrafficPercent = builder.targetTrafficPercent;
        this.stepPercent = builder.stepPercent;
        this.stepInterval = Objects.requireNonNull(builder.stepInterval, "stepInterval must not be null");
        this.errorRateThreshold = builder.errorRateThreshold;
    }

    public String getName() { return name; }
    public int getInitialTrafficPercent() { return initialTrafficPercent; }
    public int getTargetTrafficPercent() { return targetTrafficPercent; }
    public int getStepPercent() { return stepPercent; }
    public Duration getStepInterval() { return stepInterval; }
    public double getErrorRateThreshold() { return errorRateThreshold; }

    public boolean isErrorRateAcceptable(double currentErrorRate) {
        return currentErrorRate <= errorRateThreshold;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private int initialTrafficPercent = 5;
        private int targetTrafficPercent = 100;
        private int stepPercent = 10;
        private Duration stepInterval = Duration.ofMinutes(5);
        private double errorRateThreshold = 0.01;

        private Builder(String name) { this.name = name; }

        public Builder initialTrafficPercent(int v) { this.initialTrafficPercent = v; return this; }
        public Builder targetTrafficPercent(int v) { this.targetTrafficPercent = v; return this; }
        public Builder stepPercent(int v) { this.stepPercent = v; return this; }
        public Builder stepInterval(Duration d) { this.stepInterval = d; return this; }
        public Builder errorRateThreshold(double t) { this.errorRateThreshold = t; return this; }

        public CanaryPolicy build() { return new CanaryPolicy(this); }
    }

    @Override
    public String toString() {
        return String.format("CanaryPolicy{name='%s', initial=%d%%, target=%d%%, step=%d%%, interval=%s, errorThreshold=%.2f}",
                name, initialTrafficPercent, targetTrafficPercent, stepPercent, stepInterval, errorRateThreshold);
    }
}
