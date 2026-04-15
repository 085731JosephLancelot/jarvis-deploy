package com.jarvis.deploy.alert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages deployment alerts: raising, acknowledging, and querying by environment or severity.
 */
public class AlertManager {

    private final Map<String, Alert> alerts = new ConcurrentHashMap<>();

    /**
     * Raises a new alert and stores it.
     *
     * @return the created Alert
     */
    public Alert raise(String environment, AlertSeverity severity, String message) {
        Alert alert = new Alert(environment, severity, message);
        alerts.put(alert.getId(), alert);
        return alert;
    }

    /**
     * Acknowledges an alert by its ID.
     *
     * @return true if the alert was found and acknowledged, false otherwise
     */
    public boolean acknowledge(String alertId) {
        Alert alert = alerts.get(alertId);
        if (alert == null) {
            return false;
        }
        alert.acknowledge();
        return true;
    }

    /**
     * Returns all active (unacknowledged) alerts for a given environment.
     */
    public List<Alert> getActiveAlerts(String environment) {
        return alerts.values().stream()
                .filter(a -> a.getEnvironment().equals(environment) && !a.isAcknowledged())
                .sorted(Comparator.comparing(Alert::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Returns all alerts at or above the given severity threshold, regardless of environment.
     */
    public List<Alert> getAlertsBySeverity(AlertSeverity minimumSeverity) {
        return alerts.values().stream()
                .filter(a -> a.getSeverity().isAtLeast(minimumSeverity))
                .sorted(Comparator.comparing(Alert::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Returns the total number of alerts currently tracked.
     */
    public int totalAlerts() {
        return alerts.size();
    }

    /**
     * Clears all acknowledged alerts to free memory.
     */
    public void purgeAcknowledged() {
        alerts.values().removeIf(Alert::isAcknowledged);
    }
}
