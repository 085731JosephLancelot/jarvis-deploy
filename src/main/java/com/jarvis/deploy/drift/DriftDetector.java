package com.jarvis.deploy.drift;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Detects configuration or state drift between a live deployment snapshot
 * and its expected baseline configuration.
 */
public class DriftDetector {

    /**
     * Compares the expected configuration against the actual (live) configuration
     * for the given environment and returns a {@link DriftReport}.
     *
     * @param environment  the environment name
     * @param expected     the baseline / expected key-value config
     * @param actual       the live / observed key-value config
     * @return a {@link DriftReport} describing any detected drift
     */
    public DriftReport detect(String environment,
                              Map<String, String> expected,
                              Map<String, String> actual) {
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(expected, "expected config must not be null");
        Objects.requireNonNull(actual, "actual config must not be null");

        List<String> driftedKeys = new ArrayList<>();

        for (Map.Entry<String, String> entry : expected.entrySet()) {
            String key = entry.getKey();
            String expectedValue = entry.getValue();
            String actualValue = actual.get(key);
            if (!Objects.equals(expectedValue, actualValue)) {
                driftedKeys.add(key);
            }
        }

        // Keys present in actual but missing from expected are also drift
        for (String key : actual.keySet()) {
            if (!expected.containsKey(key) && !driftedKeys.contains(key)) {
                driftedKeys.add(key);
            }
        }

        DriftStatus status = driftedKeys.isEmpty() ? DriftStatus.IN_SYNC : DriftStatus.DRIFTED;
        return new DriftReport(environment, status, driftedKeys);
    }

    /**
     * Convenience method returning {@code true} when drift is detected.
     */
    public boolean hasDrift(String environment,
                            Map<String, String> expected,
                            Map<String, String> actual) {
        return detect(environment, expected, actual).hasDrift();
    }
}
