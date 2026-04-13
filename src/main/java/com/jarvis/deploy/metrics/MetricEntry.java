package com.jarvis.deploy.metrics;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable record of a single metric data point.
 */
public class MetricEntry {

    private final MetricType type;
    private final String environment;
    private final double value;
    private final Instant recordedAt;
    private final String label;

    public MetricEntry(MetricType type, String environment, double value, String label) {
        this.type = Objects.requireNonNull(type, "MetricType must not be null");
        this.environment = Objects.requireNonNull(environment, "Environment must not be null");
        this.value = value;
        this.label = label != null ? label : "";
        this.recordedAt = Instant.now();
    }

    public MetricType getType() { return type; }
    public String getEnvironment() { return environment; }
    public double getValue() { return value; }
    public Instant getRecordedAt() { return recordedAt; }
    public String getLabel() { return label; }

    @Override
    public String toString() {
        return String.format("MetricEntry{type=%s, env=%s, value=%.2f, label='%s', at=%s}",
                type, environment, value, label, recordedAt);
    }
}
