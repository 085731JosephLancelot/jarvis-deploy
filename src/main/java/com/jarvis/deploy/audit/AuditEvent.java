package com.jarvis.deploy.audit;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single audit log entry for a deployment action.
 */
public class AuditEvent {

    public enum EventType {
        DEPLOY, ROLLBACK, PROMOTE, CONFIG_CHANGE
    }

    private final String id;
    private final EventType type;
    private final String environment;
    private final String service;
    private final String version;
    private final String initiatedBy;
    private final Instant timestamp;
    private final String details;

    public AuditEvent(String id, EventType type, String environment,
                      String service, String version,
                      String initiatedBy, Instant timestamp, String details) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.service = Objects.requireNonNull(service, "service must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.initiatedBy = Objects.requireNonNull(initiatedBy, "initiatedBy must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.details = details;
    }

    public String getId() { return id; }
    public EventType getType() { return type; }
    public String getEnvironment() { return environment; }
    public String getService() { return service; }
    public String getVersion() { return version; }
    public String getInitiatedBy() { return initiatedBy; }
    public Instant getTimestamp() { return timestamp; }
    public String getDetails() { return details; }

    @Override
    public String toString() {
        return String.format("[%s] %s | env=%s service=%s version=%s by=%s | %s",
                timestamp, type, environment, service, version, initiatedBy,
                details != null ? details : "");
    }
}
