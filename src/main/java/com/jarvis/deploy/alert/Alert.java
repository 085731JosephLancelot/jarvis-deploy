package com.jarvis.deploy.alert;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a deployment alert with a severity, message, and environment context.
 */
public class Alert {

    private final String id;
    private final String environment;
    private final AlertSeverity severity;
    private final String message;
    private final Instant timestamp;
    private boolean acknowledged;

    public Alert(String environment, AlertSeverity severity, String message) {
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(message, "message must not be null");
        this.id = UUID.randomUUID().toString();
        this.environment = environment;
        this.severity = severity;
        this.message = message;
        this.timestamp = Instant.now();
        this.acknowledged = false;
    }

    public String getId() { return id; }
    public String getEnvironment() { return environment; }
    public AlertSeverity getSeverity() { return severity; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
    public boolean isAcknowledged() { return acknowledged; }

    public void acknowledge() {
        this.acknowledged = true;
    }

    @Override
    public String toString() {
        return String.format("Alert[id=%s, env=%s, severity=%s, ack=%s, msg=%s]",
                id, environment, severity, acknowledged, message);
    }
}
