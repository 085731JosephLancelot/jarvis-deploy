package com.jarvis.deploy.drift;

/**
 * Represents the drift status of a deployment relative to its baseline.
 */
public enum DriftStatus {
    IN_SYNC,
    DRIFTED,
    UNKNOWN;

    public boolean isDrifted() {
        return this == DRIFTED;
    }
}
