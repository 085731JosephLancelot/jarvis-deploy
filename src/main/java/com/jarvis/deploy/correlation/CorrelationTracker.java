package com.jarvis.deploy.correlation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Tracks active and historical deployment correlations, enabling trace lookup
 * by deployment ID, environment, or parent correlation.
 */
public class CorrelationTracker {

    private final Map<String, DeploymentCorrelation> correlationIndex = new ConcurrentHashMap<>();

    public DeploymentCorrelation register(String deploymentId, String environment) {
        DeploymentCorrelation correlation = new DeploymentCorrelation(deploymentId, environment);
        correlationIndex.put(correlation.getCorrelationId(), correlation);
        return correlation;
    }

    public DeploymentCorrelation registerChild(String deploymentId, String environment, String parentCorrelationId) {
        if (!correlationIndex.containsKey(parentCorrelationId)) {
            throw new IllegalArgumentException("Parent correlation not found: " + parentCorrelationId);
        }
        DeploymentCorrelation child = new DeploymentCorrelation(deploymentId, environment, parentCorrelationId);
        correlationIndex.put(child.getCorrelationId(), child);
        return child;
    }

    public Optional<DeploymentCorrelation> findById(String correlationId) {
        return Optional.ofNullable(correlationIndex.get(correlationId));
    }

    public List<DeploymentCorrelation> findByDeploymentId(String deploymentId) {
        return correlationIndex.values().stream()
                .filter(c -> c.getDeploymentId().equals(deploymentId))
                .collect(Collectors.toList());
    }

    public List<DeploymentCorrelation> findByEnvironment(String environment) {
        return correlationIndex.values().stream()
                .filter(c -> c.getEnvironment().equals(environment))
                .sorted(Comparator.comparing(DeploymentCorrelation::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<DeploymentCorrelation> findChildren(String parentCorrelationId) {
        return correlationIndex.values().stream()
                .filter(c -> parentCorrelationId.equals(c.getParentCorrelationId()))
                .collect(Collectors.toList());
    }

    public boolean remove(String correlationId) {
        return correlationIndex.remove(correlationId) != null;
    }

    public int size() {
        return correlationIndex.size();
    }
}
