package com.jarvis.deploy.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Collects and queries deployment metrics across environments.
 */
public class MetricsCollector {

    private final List<MetricEntry> entries = new CopyOnWriteArrayList<>();

    public void record(MetricType type, String environment, double value, String label) {
        if (type == null || environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("MetricType and environment are required");
        }
        entries.add(new MetricEntry(type, environment, value, label));
    }

    public List<MetricEntry> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public List<MetricEntry> getByType(MetricType type) {
        return entries.stream()
                .filter(e -> e.getType() == type)
                .collect(Collectors.toList());
    }

    public List<MetricEntry> getByEnvironment(String environment) {
        return entries.stream()
                .filter(e -> e.getEnvironment().equalsIgnoreCase(environment))
                .collect(Collectors.toList());
    }

    public OptionalDouble average(MetricType type, String environment) {
        return entries.stream()
                .filter(e -> e.getType() == type && e.getEnvironment().equalsIgnoreCase(environment))
                .mapToDouble(MetricEntry::getValue)
                .average();
    }

    public Map<String, Double> averageByEnvironment(MetricType type) {
        return entries.stream()
                .filter(e -> e.getType() == type)
                .collect(Collectors.groupingBy(
                        MetricEntry::getEnvironment,
                        Collectors.averagingDouble(MetricEntry::getValue)
                ));
    }

    public int count(MetricType type) {
        return (int) entries.stream().filter(e -> e.getType() == type).count();
    }

    public void clear() {
        entries.clear();
    }
}
