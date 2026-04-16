package com.jarvis.deploy.event;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeploymentEvent {

    private final String eventId;
    private final DeploymentEventType type;
    private final String deploymentId;
    private final String environment;
    private final Instant occurredAt;
    private final Map<String, String> metadata;

    public DeploymentEvent(DeploymentEventType type, String deploymentId, String environment) {
        this(type, deploymentId, environment, new HashMap<>());
    }

    public DeploymentEvent(DeploymentEventType type, String deploymentId, String environment, Map<String, String> metadata) {
        this.eventId = UUID.randomUUID().toString();
        this.type = type;
        this.deploymentId = deploymentId;
        this.environment = environment;
        this.occurredAt = Instant.now();
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    public String getEventId() { return eventId; }
    public DeploymentEventType getType() { return type; }
    public String getDeploymentId() { return deploymentId; }
    public String getEnvironment() { return environment; }
    public Instant getOccurredAt() { return occurredAt; }
    public Map<String, String> getMetadata() { return metadata; }

    @Override
    public String toString() {
        return String.format("DeploymentEvent{id=%s, type=%s, deploymentId=%s, env=%s}",
                eventId, type, deploymentId, environment);
    }
}
