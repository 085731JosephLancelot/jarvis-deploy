package com.jarvis.deploy.signal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentSignalTest {

    @Test
    void shouldCreateSignalWithRequiredFields() {
        DeploymentSignal signal = new DeploymentSignal("dep-001", SignalType.PAUSE, "admin", "Manual pause");

        assertEquals("dep-001", signal.getDeploymentId());
        assertEquals(SignalType.PAUSE, signal.getType());
        assertEquals("admin", signal.getIssuedBy());
        assertEquals("Manual pause", signal.getReason());
        assertNotNull(signal.getIssuedAt());
        assertFalse(signal.isAcknowledged());
    }

    @Test
    void shouldDefaultReasonToEmptyStringWhenNull() {
        DeploymentSignal signal = new DeploymentSignal("dep-002", SignalType.ABORT, "ci-bot", null);

        assertEquals("", signal.getReason());
    }

    @Test
    void shouldAcknowledgeSignal() {
        DeploymentSignal signal = new DeploymentSignal("dep-003", SignalType.RESUME, "operator", "Resuming after review");

        assertFalse(signal.isAcknowledged());
        signal.acknowledge();
        assertTrue(signal.isAcknowledged());
    }

    @Test
    void shouldThrowWhenDeploymentIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                new DeploymentSignal(null, SignalType.PAUSE, "admin", "reason"));
    }

    @Test
    void shouldThrowWhenTypeIsNull() {
        assertThrows(NullPointerException.class, () ->
                new DeploymentSignal("dep-004", null, "admin", "reason"));
    }

    @Test
    void shouldThrowWhenIssuedByIsNull() {
        assertThrows(NullPointerException.class, () ->
                new DeploymentSignal("dep-005", SignalType.ABORT, null, "reason"));
    }

    @Test
    void shouldIncludeAllFieldsInToString() {
        DeploymentSignal signal = new DeploymentSignal("dep-006", SignalType.ABORT, "admin", "Emergency stop");
        String str = signal.toString();

        assertTrue(str.contains("dep-006"));
        assertTrue(str.contains("ABORT"));
        assertTrue(str.contains("admin"));
        assertTrue(str.contains("Emergency stop"));
    }

    @Test
    void shouldSupportAllSignalTypes() {
        for (SignalType type : SignalType.values()) {
            DeploymentSignal signal = new DeploymentSignal("dep-007", type, "system", "");
            assertEquals(type, signal.getType());
        }
    }
}
