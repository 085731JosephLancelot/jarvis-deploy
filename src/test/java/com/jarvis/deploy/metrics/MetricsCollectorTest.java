package com.jarvis.deploy.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;

class MetricsCollectorTest {

    private MetricsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new MetricsCollector();
    }

    @Test
    void shouldRecordAndRetrieveMetric() {
        collector.record(MetricType.DEPLOYMENT_DURATION, "production", 120.5, "deploy-v1");
        List<MetricEntry> all = collector.getAll();
        assertEquals(1, all.size());
        assertEquals(MetricType.DEPLOYMENT_DURATION, all.get(0).getType());
        assertEquals("production", all.get(0).getEnvironment());
        assertEquals(120.5, all.get(0).getValue());
    }

    @Test
    void shouldFilterByType() {
        collector.record(MetricType.DEPLOYMENT_DURATION, "staging", 90.0, "v2");
        collector.record(MetricType.ROLLBACK_COUNT, "staging", 1.0, "rollback");
        List<MetricEntry> results = collector.getByType(MetricType.DEPLOYMENT_DURATION);
        assertEquals(1, results.size());
        assertEquals(MetricType.DEPLOYMENT_DURATION, results.get(0).getType());
    }

    @Test
    void shouldFilterByEnvironment() {
        collector.record(MetricType.FAILURE_COUNT, "production", 3.0, null);
        collector.record(MetricType.FAILURE_COUNT, "staging", 1.0, null);
        List<MetricEntry> results = collector.getByEnvironment("production");
        assertEquals(1, results.size());
        assertEquals("production", results.get(0).getEnvironment());
    }

    @Test
    void shouldCalculateAverageForTypeAndEnvironment() {
        collector.record(MetricType.HEALTH_CHECK_LATENCY, "production", 200.0, null);
        collector.record(MetricType.HEALTH_CHECK_LATENCY, "production", 400.0, null);
        OptionalDouble avg = collector.average(MetricType.HEALTH_CHECK_LATENCY, "production");
        assertTrue(avg.isPresent());
        assertEquals(300.0, avg.getAsDouble(), 0.001);
    }

    @Test
    void shouldReturnEmptyAverageWhenNoEntries() {
        OptionalDouble avg = collector.average(MetricType.ROLLBACK_COUNT, "dev");
        assertFalse(avg.isPresent());
    }

    @Test
    void shouldGroupAverageByEnvironment() {
        collector.record(MetricType.DEPLOYMENT_DURATION, "production", 100.0, null);
        collector.record(MetricType.DEPLOYMENT_DURATION, "production", 200.0, null);
        collector.record(MetricType.DEPLOYMENT_DURATION, "staging", 50.0, null);
        Map<String, Double> avgs = collector.averageByEnvironment(MetricType.DEPLOYMENT_DURATION);
        assertEquals(150.0, avgs.get("production"), 0.001);
        assertEquals(50.0, avgs.get("staging"), 0.001);
    }

    @Test
    void shouldCountByType() {
        collector.record(MetricType.ROLLBACK_COUNT, "production", 1.0, null);
        collector.record(MetricType.ROLLBACK_COUNT, "staging", 2.0, null);
        assertEquals(2, collector.count(MetricType.ROLLBACK_COUNT));
        assertEquals(0, collector.count(MetricType.FAILURE_COUNT));
    }

    @Test
    void shouldThrowOnNullType() {
        assertThrows(IllegalArgumentException.class,
                () -> collector.record(null, "production", 1.0, null));
    }

    @Test
    void shouldThrowOnBlankEnvironment() {
        assertThrows(IllegalArgumentException.class,
                () -> collector.record(MetricType.SUCCESS_RATE, "  ", 99.0, null));
    }

    @Test
    void shouldClearAllEntries() {
        collector.record(MetricType.FAILURE_COUNT, "dev", 5.0, null);
        collector.clear();
        assertTrue(collector.getAll().isEmpty());
    }
}
