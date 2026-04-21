package com.jarvis.deploy.checkpoint;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry for managing deployment checkpoints across deployments.
 * Supports registration, retrieval, and status querying.
 */
public class CheckpointRegistry {

    private final Map<String, DeploymentCheckpoint> checkpoints = new ConcurrentHashMap<>();

    public void register(DeploymentCheckpoint checkpoint) {
        Objects.requireNonNull(checkpoint, "checkpoint must not be null");
        checkpoints.put(checkpoint.getId(), checkpoint);
    }

    public Optional<DeploymentCheckpoint> findById(String id) {
        return Optional.ofNullable(checkpoints.get(id));
    }

    public List<DeploymentCheckpoint> findByDeploymentId(String deploymentId) {
        Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        return checkpoints.values().stream()
                .filter(cp -> deploymentId.equals(cp.getDeploymentId()))
                .collect(Collectors.toList());
    }

    public List<DeploymentCheckpoint> findByStatus(CheckpointStatus status) {
        Objects.requireNonNull(status, "status must not be null");
        return checkpoints.values().stream()
                .filter(cp -> cp.getStatus() == status)
                .collect(Collectors.toList());
    }

    public boolean allPassed(String deploymentId) {
        List<DeploymentCheckpoint> deploymentCheckpoints = findByDeploymentId(deploymentId);
        if (deploymentCheckpoints.isEmpty()) {
            return false;
        }
        return deploymentCheckpoints.stream()
                .allMatch(cp -> cp.isPassed() || cp.getStatus() == CheckpointStatus.SKIPPED);
    }

    public boolean anyFailed(String deploymentId) {
        return findByDeploymentId(deploymentId).stream()
                .anyMatch(DeploymentCheckpoint::isFailed);
    }

    public void remove(String id) {
        checkpoints.remove(id);
    }

    public void clearForDeployment(String deploymentId) {
        findByDeploymentId(deploymentId)
                .forEach(cp -> checkpoints.remove(cp.getId()));
    }

    public int size() {
        return checkpoints.size();
    }
}
