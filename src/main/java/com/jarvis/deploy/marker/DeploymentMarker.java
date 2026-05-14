package com.jarvis.deploy.marker;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a named marker attached to a deployment at a specific point in time.
 * Markers can carry arbitrary metadata and are used to annotate significant
 * deployment events (e.g. "smoke-tests-passed", "canary-promoted").
 */
public class DeploymentMarker {

    private final String deploymentId;
    private final String name;
    private final Instant createdAt;
    private final Map<String, String> metadata;

    public DeploymentMarker(String deploymentId, String name, Map<String, String> metadata) {
        if (deploymentId == null || deploymentId.isBlank()) {
            throw new IllegalArgumentException("deploymentId must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.deploymentId = deploymentId;
        this.name = name;
        this.createdAt = Instant.now();
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }

    public DeploymentMarker(String deploymentId, String name) {
        this(deploymentId, name, null);
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    public String getMetadataValue(String key) {
        return metadata.get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentMarker)) return false;
        DeploymentMarker that = (DeploymentMarker) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
               Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, name);
    }

    @Override
    public String toString() {
        return "DeploymentMarker{deploymentId='" + deploymentId + "', name='" + name +
               "', createdAt=" + createdAt + ", metadata=" + metadata + "}";
    }
}
