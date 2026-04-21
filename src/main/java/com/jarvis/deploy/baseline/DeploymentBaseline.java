package com.jarvis.deploy.baseline;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a captured baseline state of a deployment environment,
 * used for drift detection and compliance verification.
 */
public class DeploymentBaseline {

    private final String id;
    private final String environmentId;
    private final String version;
    private final Map<String, String> configSnapshot;
    private final Instant capturedAt;
    private BaselineStatus status;

    public DeploymentBaseline(String id, String environmentId, String version,
                               Map<String, String> configSnapshot) {
        this.id = Objects.requireNonNull(id, "Baseline id must not be null");
        this.environmentId = Objects.requireNonNull(environmentId, "Environment id must not be null");
        this.version = Objects.requireNonNull(version, "Version must not be null");
        this.configSnapshot = Collections.unmodifiableMap(new HashMap<>(configSnapshot));
        this.capturedAt = Instant.now();
        this.status = BaselineStatus.ACTIVE;
    }

    public String getId() { return id; }
    public String getEnvironmentId() { return environmentId; }
    public String getVersion() { return version; }
    public Map<String, String> getConfigSnapshot() { return configSnapshot; }
    public Instant getCapturedAt() { return capturedAt; }
    public BaselineStatus getStatus() { return status; }

    public void archive() {
        this.status = BaselineStatus.ARCHIVED;
    }

    public boolean isDriftedFrom(Map<String, String> currentConfig) {
        if (currentConfig == null) return true;
        return !configSnapshot.equals(currentConfig);
    }

    public Map<String, String> computeDiff(Map<String, String> currentConfig) {
        Map<String, String> diffs = new HashMap<>();
        for (Map.Entry<String, String> entry : configSnapshot.entrySet()) {
            String current = currentConfig.get(entry.getKey());
            if (!entry.getValue().equals(current)) {
                diffs.put(entry.getKey(), entry.getValue() + " -> " + current);
            }
        }
        for (String key : currentConfig.keySet()) {
            if (!configSnapshot.containsKey(key)) {
                diffs.put(key, "<absent> -> " + currentConfig.get(key));
            }
        }
        return Collections.unmodifiableMap(diffs);
    }

    @Override
    public String toString() {
        return "DeploymentBaseline{id='" + id + "', env='" + environmentId +
               "', version='" + version + "', status=" + status + "}";
    }
}
