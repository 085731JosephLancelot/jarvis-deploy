package com.jarvis.deploy.baseline;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages deployment baselines per environment, supporting capture,
 * retrieval, drift detection, and archival.
 */
public class BaselineManager {

    private final Map<String, DeploymentBaseline> baselines = new ConcurrentHashMap<>();

    public DeploymentBaseline capture(String environmentId, String version,
                                       Map<String, String> configSnapshot) {
        Objects.requireNonNull(environmentId, "Environment id must not be null");
        Objects.requireNonNull(version, "Version must not be null");
        Objects.requireNonNull(configSnapshot, "Config snapshot must not be null");

        String id = UUID.randomUUID().toString();
        DeploymentBaseline baseline = new DeploymentBaseline(id, environmentId, version, configSnapshot);

        // Archive previous active baseline for this environment
        getActiveBaseline(environmentId).ifPresent(DeploymentBaseline::archive);

        baselines.put(id, baseline);
        return baseline;
    }

    public Optional<DeploymentBaseline> getActiveBaseline(String environmentId) {
        return baselines.values().stream()
                .filter(b -> b.getEnvironmentId().equals(environmentId))
                .filter(b -> b.getStatus() == BaselineStatus.ACTIVE)
                .findFirst();
    }

    public Optional<DeploymentBaseline> getById(String id) {
        return Optional.ofNullable(baselines.get(id));
    }

    public List<DeploymentBaseline> listByEnvironment(String environmentId) {
        return baselines.values().stream()
                .filter(b -> b.getEnvironmentId().equals(environmentId))
                .sorted(Comparator.comparing(DeploymentBaseline::getCapturedAt).reversed())
                .collect(Collectors.toList());
    }

    public Optional<Map<String, String>> detectDrift(String environmentId,
                                                      Map<String, String> currentConfig) {
        return getActiveBaseline(environmentId).map(baseline -> {
            if (baseline.isDriftedFrom(currentConfig)) {
                return baseline.computeDiff(currentConfig);
            }
            return Collections.<String, String>emptyMap();
        });
    }

    public boolean archiveBaseline(String id) {
        DeploymentBaseline baseline = baselines.get(id);
        if (baseline == null) return false;
        baseline.archive();
        return true;
    }

    public int count() {
        return baselines.size();
    }
}
