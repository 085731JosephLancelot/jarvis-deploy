package com.jarvis.deploy.dependency;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry for managing deployment dependencies.
 * Supports adding, removing, and resolving dependency chains with cycle detection.
 */
public class DependencyRegistry {

    private final Map<String, DeploymentDependency> dependencies = new LinkedHashMap<>();

    public void register(DeploymentDependency dependency) {
        Objects.requireNonNull(dependency, "Dependency must not be null");
        if (wouldCreateCycle(dependency.getSourceDeploymentId(), dependency.getRequiredDeploymentId())) {
            throw new IllegalStateException(
                "Registering dependency would create a cycle: " + dependency);
        }
        dependencies.put(dependency.getId(), dependency);
    }

    public boolean remove(String dependencyId) {
        return dependencies.remove(dependencyId) != null;
    }

    public Optional<DeploymentDependency> findById(String dependencyId) {
        return Optional.ofNullable(dependencies.get(dependencyId));
    }

    public List<DeploymentDependency> getDependenciesFor(String deploymentId) {
        return dependencies.values().stream()
                .filter(d -> d.getSourceDeploymentId().equals(deploymentId))
                .collect(Collectors.toList());
    }

    public List<DeploymentDependency> getDependentsOf(String deploymentId) {
        return dependencies.values().stream()
                .filter(d -> d.getRequiredDeploymentId().equals(deploymentId))
                .collect(Collectors.toList());
    }

    public List<String> getOrderedDeploymentIds(String startId) {
        List<String> ordered = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>();
        resolveOrder(startId, visited, ordered);
        return Collections.unmodifiableList(ordered);
    }

    private void resolveOrder(String deploymentId, Set<String> visited, List<String> ordered) {
        if (!visited.add(deploymentId)) return;
        for (DeploymentDependency dep : getDependenciesFor(deploymentId)) {
            resolveOrder(dep.getRequiredDeploymentId(), visited, ordered);
        }
        ordered.add(deploymentId);
    }

    private boolean wouldCreateCycle(String source, String required) {
        Set<String> reachable = new HashSet<>();
        collectReachable(source, reachable);
        return reachable.contains(required);
    }

    private void collectReachable(String deploymentId, Set<String> reachable) {
        for (DeploymentDependency dep : getDependentsOf(deploymentId)) {
            if (reachable.add(dep.getSourceDeploymentId())) {
                collectReachable(dep.getSourceDeploymentId(), reachable);
            }
        }
    }

    public int size() { return dependencies.size(); }

    public void clear() { dependencies.clear(); }
}
