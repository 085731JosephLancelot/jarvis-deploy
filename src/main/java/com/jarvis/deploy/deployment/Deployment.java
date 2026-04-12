package com.jarvis.deploy.deployment;

import com.jarvis.deploy.environment.Environment;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single deployment operation targeting a specific environment.
 */
public class Deployment {

    private final String id;
    private final String artifactVersion;
    private final Environment environment;
    private final Instant createdAt;
    private DeploymentStatus status;
    private Instant completedAt;

    public Deployment(String artifactVersion, Environment environment) {
        Objects.requireNonNull(artifactVersion, "artifactVersion must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        this.id = UUID.randomUUID().toString();
        this.artifactVersion = artifactVersion;
        this.environment = environment;
        this.createdAt = Instant.now();
        this.status = DeploymentStatus.PENDING;
    }

    public String getId() { return id; }

    public String getArtifactVersion() { return artifactVersion; }

    public Environment getEnvironment() { return environment; }

    public Instant getCreatedAt() { return createdAt; }

    public DeploymentStatus getStatus() { return status; }

    public Instant getCompletedAt() { return completedAt; }

    public void setStatus(DeploymentStatus status) {
        Objects.requireNonNull(status, "status must not be null");
        this.status = status;
        if (status.isTerminal()) {
            this.completedAt = Instant.now();
        }
    }

    @Override
    public String toString() {
        return String.format("Deployment{id='%s', version='%s', env='%s', status=%s}",
                id, artifactVersion, environment.getName(), status);
    }
}
