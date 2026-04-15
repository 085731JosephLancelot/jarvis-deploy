package com.jarvis.deploy.alert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlertManagerTest {

    private AlertManager alertManager;

    @BeforeEach
    void setUp() {
        alertManager = new AlertManager();
    }

    @Test
    void raiseAlert_storesAlertAndReturnsIt() {
        Alert alert = alertManager.raise("production", AlertSeverity.CRITICAL, "Deployment failed");
        assertNotNull(alert);
        assertNotNull(alert.getId());
        assertEquals("production", alert.getEnvironment());
        assertEquals(AlertSeverity.CRITICAL, alert.getSeverity());
        assertEquals("Deployment failed", alert.getMessage());
        assertFalse(alert.isAcknowledged());
        assertEquals(1, alertManager.totalAlerts());
    }

    @Test
    void acknowledge_marksAlertAsAcknowledged() {
        Alert alert = alertManager.raise("staging", AlertSeverity.WARNING, "High memory usage");
        boolean result = alertManager.acknowledge(alert.getId());
        assertTrue(result);
        assertTrue(alert.isAcknowledged());
    }

    @Test
    void acknowledge_returnsFalseForUnknownId() {
        boolean result = alertManager.acknowledge("nonexistent-id");
        assertFalse(result);
    }

    @Test
    void getActiveAlerts_excludesAcknowledgedAlerts() {
        Alert a1 = alertManager.raise("production", AlertSeverity.CRITICAL, "Error A");
        Alert a2 = alertManager.raise("production", AlertSeverity.WARNING, "Warning B");
        alertManager.acknowledge(a1.getId());

        List<Alert> active = alertManager.getActiveAlerts("production");
        assertEquals(1, active.size());
        assertEquals(a2.getId(), active.get(0).getId());
    }

    @Test
    void getActiveAlerts_filtersbyEnvironment() {
        alertManager.raise("production", AlertSeverity.INFO, "Info prod");
        alertManager.raise("staging", AlertSeverity.WARNING, "Warn staging");

        List<Alert> prodAlerts = alertManager.getActiveAlerts("production");
        assertEquals(1, prodAlerts.size());
        assertEquals("production", prodAlerts.get(0).getEnvironment());
    }

    @Test
    void getAlertsBySeverity_returnsAlertsAtOrAboveThreshold() {
        alertManager.raise("production", AlertSeverity.INFO, "Info message");
        alertManager.raise("production", AlertSeverity.WARNING, "Warning message");
        alertManager.raise("production", AlertSeverity.CRITICAL, "Critical message");

        List<Alert> warnings = alertManager.getAlertsBySeverity(AlertSeverity.WARNING);
        assertEquals(2, warnings.size());
        assertTrue(warnings.stream().noneMatch(a -> a.getSeverity() == AlertSeverity.INFO));
    }

    @Test
    void purgeAcknowledged_removesOnlyAcknowledgedAlerts() {
        Alert a1 = alertManager.raise("production", AlertSeverity.CRITICAL, "Crit");
        Alert a2 = alertManager.raise("production", AlertSeverity.INFO, "Info");
        alertManager.acknowledge(a1.getId());

        alertManager.purgeAcknowledged();

        assertEquals(1, alertManager.totalAlerts());
        assertFalse(alertManager.getActiveAlerts("production").isEmpty());
        assertEquals(a2.getId(), alertManager.getActiveAlerts("production").get(0).getId());
    }

    @Test
    void raise_requiresNonNullArguments() {
        assertThrows(NullPointerException.class, () -> alertManager.raise(null, AlertSeverity.INFO, "msg"));
        assertThrows(NullPointerException.class, () -> alertManager.raise("env", null, "msg"));
        assertThrows(NullPointerException.class, () -> alertManager.raise("env", AlertSeverity.INFO, null));
    }
}
