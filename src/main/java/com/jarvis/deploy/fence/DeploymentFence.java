package com.jarvis.deploy.fence;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a deployment fence — a named boundary that restricts which
 * environments or services are allowed to proceed with a deployment.
 */
public class DeploymentFence {

    private final String fenceId;
    private final String name;
    private final Set<String> allowedEnvironments;
    private final Set<String> blockedServices;
    private final Instant createdAt;
    private boolean active;

    public DeploymentFence(String fenceId, String name) {
        this.fenceId = Objects.requireNonNull(fenceId, "fenceId must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.allowedEnvironments = new HashSet<>();
        this.blockedServices = new HashSet<>();
        this.createdAt = Instant.now();
        this.active = true;
    }

    public void allowEnvironment(String environment) {
        if (environment != null && !environment.isBlank()) {
            allowedEnvironments.add(environment);
        }
    }

    public void blockService(String service) {
        if (service != null && !service.isBlank()) {
            blockedServices.add(service);
        }
    }

    public boolean permits(String environment, String service) {
        if (!active) return false;
        if (!allowedEnvironments.isEmpty() && !allowedEnvironments.contains(environment)) {
            return false;
        }
        return !blockedServices.contains(service);
    }

    public void deactivate() {
        this.active = false;
    }

    public String getFenceId() { return fenceId; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Set<String> getAllowedEnvironments() { return Collections.unmodifiableSet(allowedEnvironments); }
    public Set<String> getBlockedServices() { return Collections.unmodifiableSet(blockedServices); }

    @Override
    public String toString() {
        return "DeploymentFence{fenceId='" + fenceId + "', name='" + name +
                "', active=" + active + ", allowedEnvironments=" + allowedEnvironments +
                ", blockedServices=" + blockedServices + "}";
    }
}
