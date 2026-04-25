package com.jarvis.deploy.drift;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Captures the result of a drift detection scan for a specific environment.
 */
public class DriftReport {

    private final String environment;
    private final DriftStatus status;
    private final List<String> driftedKeys;
    private final Instant detectedAt;

    public DriftReport(String environment, DriftStatus status, List<String> driftedKeys) {
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.driftedKeys = driftedKeys != null ? List.copyOf(driftedKeys) : Collections.emptyList();
        this.detectedAt = Instant.now();
    }

    public String getEnvironment() { return environment; }

    public DriftStatus getStatus() { return status; }

    public List<String> getDriftedKeys() { return driftedKeys; }

    public Instant getDetectedAt() { return detectedAt; }

    public boolean hasDrift() { return status == DriftStatus.DRIFTED; }

    @Override
    public String toString() {
        return "DriftReport{environment='" + environment + "', status=" + status +
               ", driftedKeys=" + driftedKeys + ", detectedAt=" + detectedAt + '}';
    }
}
