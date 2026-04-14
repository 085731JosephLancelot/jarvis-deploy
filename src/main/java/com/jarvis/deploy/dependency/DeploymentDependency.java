package com.jarvis.deploy.dependency;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a dependency relationship between two deployments.
 * A deployment may require another deployment to be in a specific state before proceeding.
 */
public class DeploymentDependency {

    private final String id;
    private final String sourceDeploymentId;
    private final String requiredDeploymentId;
    private final DependencyType type;
    private final Instant createdAt;

    public DeploymentDependency(String id, String sourceDeploymentId,
                                String requiredDeploymentId, DependencyType type) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Dependency id must not be blank");
        if (sourceDeploymentId == null || sourceDeploymentId.isBlank())
            throw new IllegalArgumentException("Source deployment id must not be blank");
        if (requiredDeploymentId == null || requiredDeploymentId.isBlank())
            throw new IllegalArgumentException("Required deployment id must not be blank");
        if (sourceDeploymentId.equals(requiredDeploymentId))
            throw new IllegalArgumentException("A deployment cannot depend on itself");
        this.id = id;
        this.sourceDeploymentId = sourceDeploymentId;
        this.requiredDeploymentId = requiredDeploymentId;
        this.type = Objects.requireNonNull(type, "Dependency type must not be null");
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getSourceDeploymentId() { return sourceDeploymentId; }
    public String getRequiredDeploymentId() { return requiredDeploymentId; }
    public DependencyType getType() { return type; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentDependency)) return false;
        DeploymentDependency that = (DeploymentDependency) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "DeploymentDependency{id='" + id + "', source='" + sourceDeploymentId +
               "', required='" + requiredDeploymentId + "', type=" + type + "}";
    }
}
