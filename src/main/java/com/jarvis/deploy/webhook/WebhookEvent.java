package com.jarvis.deploy.webhook;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a webhook event dispatched during deployment lifecycle.
 */
public class WebhookEvent {

    private final String id;
    private final WebhookEventType type;
    private final String deploymentId;
    private final String environment;
    private final Instant occurredAt;
    private final Map<String, String> payload;

    public WebhookEvent(WebhookEventType type, String deploymentId, String environment,
                        Map<String, String> payload) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.deploymentId = deploymentId;
        this.environment = environment;
        this.occurredAt = Instant.now();
        this.payload = Collections.unmodifiableMap(new HashMap<>(payload));
    }

    public String getId() { return id; }
    public WebhookEventType getType() { return type; }
    public String getDeploymentId() { return deploymentId; }
    public String getEnvironment() { return environment; }
    public Instant getOccurredAt() { return occurredAt; }
    public Map<String, String> getPayload() { return payload; }

    @Override
    public String toString() {
        return "WebhookEvent{id='" + id + "', type=" + type +
               ", deploymentId='" + deploymentId + "', environment='" + environment +
               "', occurredAt=" + occurredAt + "}";
    }
}
