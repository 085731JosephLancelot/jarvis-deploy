package com.jarvis.deploy.alert;

/**
 * Severity levels for deployment alerts.
 */
public enum AlertSeverity {
    INFO,
    WARNING,
    CRITICAL;

    public boolean isAtLeast(AlertSeverity other) {
        return this.ordinal() >= other.ordinal();
    }
}
