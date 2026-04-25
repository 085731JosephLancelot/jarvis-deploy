package com.jarvis.deploy.drift;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains a registry of baseline configurations per environment and
 * provides a scanning facility to produce drift reports on demand.
 */
public class DriftMonitor {

    private final DriftDetector detector;
    private final Map<String, Map<String, String>> baselines = new ConcurrentHashMap<>();
    private final List<DriftReport> history = Collections.synchronizedList(new ArrayList<>());

    public DriftMonitor(DriftDetector detector) {
        this.detector = Objects.requireNonNull(detector, "detector must not be null");
    }

    /**
     * Registers or replaces the baseline configuration for an environment.
     */
    public void registerBaseline(String environment, Map<String, String> baseline) {
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(baseline, "baseline must not be null");
        baselines.put(environment, Map.copyOf(baseline));
    }

    /**
     * Scans the given live configuration against the registered baseline and
     * records the result in history.
     *
     * @return the resulting {@link DriftReport}
     * @throws IllegalStateException if no baseline is registered for the environment
     */
    public DriftReport scan(String environment, Map<String, String> actual) {
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(actual, "actual config must not be null");

        Map<String, String> baseline = baselines.get(environment);
        if (baseline == null) {
            throw new IllegalStateException("No baseline registered for environment: " + environment);
        }

        DriftReport report = detector.detect(environment, baseline, actual);
        history.add(report);
        return report;
    }

    public List<DriftReport> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public boolean hasBaseline(String environment) {
        return baselines.containsKey(environment);
    }

    public void clearHistory() {
        history.clear();
    }
}
