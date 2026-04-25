package com.jarvis.deploy.drift;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DriftMonitorTest {

    private DriftDetector detector;
    private DriftMonitor monitor;

    @BeforeEach
    void setUp() {
        detector = new DriftDetector();
        monitor = new DriftMonitor(detector);
    }

    @Test
    void registerAndScan_noChanges_returnsInSync() {
        Map<String, String> baseline = Map.of("key1", "val1", "key2", "val2");
        monitor.registerBaseline("staging", baseline);

        DriftReport report = monitor.scan("staging", Map.of("key1", "val1", "key2", "val2"));

        assertEquals(DriftStatus.IN_SYNC, report.getStatus());
        assertFalse(report.hasDrift());
        assertTrue(report.getDriftedKeys().isEmpty());
    }

    @Test
    void scan_withChangedValue_reportsDrift() {
        monitor.registerBaseline("production", Map.of("replicas", "3", "image", "v1.0"));

        DriftReport report = monitor.scan("production", Map.of("replicas", "5", "image", "v1.0"));

        assertEquals(DriftStatus.DRIFTED, report.getStatus());
        assertTrue(report.hasDrift());
        assertTrue(report.getDriftedKeys().contains("replicas"));
        assertFalse(report.getDriftedKeys().contains("image"));
    }

    @Test
    void scan_withExtraKeyInActual_reportsDrift() {
        monitor.registerBaseline("dev", Map.of("key1", "a"));

        DriftReport report = monitor.scan("dev", Map.of("key1", "a", "extra", "b"));

        assertTrue(report.hasDrift());
        assertTrue(report.getDriftedKeys().contains("extra"));
    }

    @Test
    void scan_withMissingKeyInActual_reportsDrift() {
        monitor.registerBaseline("dev", Map.of("key1", "a", "key2", "b"));

        DriftReport report = monitor.scan("dev", Map.of("key1", "a"));

        assertTrue(report.hasDrift());
        assertTrue(report.getDriftedKeys().contains("key2"));
    }

    @Test
    void scan_withoutBaseline_throwsException() {
        assertThrows(IllegalStateException.class,
                () -> monitor.scan("unknown", Map.of("k", "v")));
    }

    @Test
    void history_recordsAllScans() {
        monitor.registerBaseline("staging", Map.of("k", "v"));
        monitor.scan("staging", Map.of("k", "v"));
        monitor.scan("staging", Map.of("k", "changed"));

        List<DriftReport> history = monitor.getHistory();
        assertEquals(2, history.size());
        assertEquals(DriftStatus.IN_SYNC, history.get(0).getStatus());
        assertEquals(DriftStatus.DRIFTED, history.get(1).getStatus());
    }

    @Test
    void clearHistory_removesAllEntries() {
        monitor.registerBaseline("staging", Map.of("k", "v"));
        monitor.scan("staging", Map.of("k", "v"));
        monitor.clearHistory();

        assertTrue(monitor.getHistory().isEmpty());
    }

    @Test
    void hasBaseline_returnsTrueAfterRegistration() {
        assertFalse(monitor.hasBaseline("prod"));
        monitor.registerBaseline("prod", Map.of("k", "v"));
        assertTrue(monitor.hasBaseline("prod"));
    }
}
