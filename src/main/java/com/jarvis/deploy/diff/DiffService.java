package com.jarvis.deploy.diff;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Computes a {@link DeploymentDiff} between two configuration snapshots
 * represented as key-value maps.
 */
public class DiffService {

    /**
     * Generates a diff between {@code fromConfig} (old) and {@code toConfig} (new).
     *
     * @param fromVersion label for the source version
     * @param toVersion   label for the target version
     * @param environment target environment name
     * @param fromConfig  configuration map of the source deployment
     * @param toConfig    configuration map of the target deployment
     * @return a {@link DeploymentDiff} describing all changes
     */
    public DeploymentDiff compute(String fromVersion, String toVersion, String environment,
                                  Map<String, String> fromConfig,
                                  Map<String, String> toConfig) {
        Objects.requireNonNull(fromConfig, "fromConfig must not be null");
        Objects.requireNonNull(toConfig, "toConfig must not be null");

        Map<String, String> added = new HashMap<>();
        Map<String, String> removed = new HashMap<>();
        Map<String, DeploymentDiff.DiffEntry> changed = new HashMap<>();

        // Keys present in toConfig but not in fromConfig → added
        for (Map.Entry<String, String> entry : toConfig.entrySet()) {
            if (!fromConfig.containsKey(entry.getKey())) {
                added.put(entry.getKey(), entry.getValue());
            }
        }

        // Keys present in fromConfig but not in toConfig → removed
        for (Map.Entry<String, String> entry : fromConfig.entrySet()) {
            if (!toConfig.containsKey(entry.getKey())) {
                removed.put(entry.getKey(), entry.getValue());
            }
        }

        // Keys present in both but with different values → changed
        for (Map.Entry<String, String> entry : fromConfig.entrySet()) {
            String key = entry.getKey();
            if (toConfig.containsKey(key)) {
                String oldVal = entry.getValue();
                String newVal = toConfig.get(key);
                if (!Objects.equals(oldVal, newVal)) {
                    changed.put(key, new DeploymentDiff.DiffEntry(oldVal, newVal));
                }
            }
        }

        return new DeploymentDiff(fromVersion, toVersion, environment, added, removed, changed);
    }

    /**
     * Formats a diff as a human-readable summary string suitable for CLI output.
     */
    public String format(DeploymentDiff diff) {
        if (diff.isEmpty()) {
            return String.format("No changes between %s and %s in [%s]",
                    diff.getFromVersion(), diff.getToVersion(), diff.getEnvironment());
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Diff: %s -> %s  [%s]  (%d change(s))%n",
                diff.getFromVersion(), diff.getToVersion(), diff.getEnvironment(), diff.totalChanges()));
        diff.getAdded().forEach((k, v) -> sb.append(String.format("  + %-30s = %s%n", k, v)));
        diff.getRemoved().forEach((k, v) -> sb.append(String.format("  - %-30s = %s%n", k, v)));
        diff.getChanged().forEach((k, e) -> sb.append(String.format("  ~ %-30s : %s -> %s%n", k, e.getBefore(), e.getAfter())));
        return sb.toString().stripTrailing();
    }
}
