package com.jarvis.deploy.notification;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single notification event generated during a deployment lifecycle.
 */
public class NotificationEvent {

    private final String deploymentId;
    private final String environment;
    private final String message;
    private final NotificationLevel level;
    private final Instant timestamp;

    public NotificationEvent(String deploymentId, String environment,
                             String message, NotificationLevel level) {
        this.deploymentId = Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        this.environment  = Objects.requireNonNull(environment,  "environment must not be null");
        this.message      = Objects.requireNonNull(message,      "message must not be null");
        this.level        = Objects.requireNonNull(level,        "level must not be null");
        this.timestamp    = Instant.now();
    }

    public String getDeploymentId()  { return deploymentId; }
    public String getEnvironment()   { return environment; }
    public String getMessage()       { return message; }
    public NotificationLevel getLevel() { return level; }
    public Instant getTimestamp()    { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s | env=%s | deployment=%s | %s",
                level, timestamp, environment, deploymentId, message);
    }
}
