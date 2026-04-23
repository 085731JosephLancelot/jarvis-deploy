package com.jarvis.deploy.badge;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a deployment badge that captures status metadata
 * for a given environment and service combination.
 */
public class DeploymentBadge {

    private final String serviceId;
    private final String environment;
    private BadgeStatus status;
    private String version;
    private Instant lastUpdated;
    private String label;

    public DeploymentBadge(String serviceId, String environment, String version) {
        Objects.requireNonNull(serviceId, "serviceId must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(version, "version must not be null");
        this.serviceId = serviceId;
        this.environment = environment;
        this.version = version;
        this.status = BadgeStatus.UNKNOWN;
        this.lastUpdated = Instant.now();
        this.label = serviceId + ":" + environment;
    }

    public void update(BadgeStatus status, String version) {
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(version, "version must not be null");
        this.status = status;
        this.version = version;
        this.lastUpdated = Instant.now();
    }

    public String getServiceId() { return serviceId; }
    public String getEnvironment() { return environment; }
    public BadgeStatus getStatus() { return status; }
    public String getVersion() { return version; }
    public Instant getLastUpdated() { return lastUpdated; }
    public String getLabel() { return label; }

    public void setLabel(String label) {
        Objects.requireNonNull(label, "label must not be null");
        this.label = label;
    }

    @Override
    public String toString() {
        return "DeploymentBadge{serviceId='" + serviceId + "', environment='" + environment +
                "', status=" + status + ", version='" + version + "', lastUpdated=" + lastUpdated + "}";
    }
}
