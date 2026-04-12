package com.jarvis.deploy.notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service responsible for dispatching and storing deployment notifications.
 * Notifications below the configured minimum level are silently dropped.
 */
public class NotificationService {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());

    private final NotificationLevel minimumLevel;
    private final List<NotificationEvent> history = new ArrayList<>();

    public NotificationService(NotificationLevel minimumLevel) {
        if (minimumLevel == null) {
            throw new IllegalArgumentException("minimumLevel must not be null");
        }
        this.minimumLevel = minimumLevel;
    }

    /**
     * Sends a notification if its level meets or exceeds the configured minimum.
     *
     * @param event the notification event to dispatch
     */
    public void send(NotificationEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        if (event.getLevel().isAtLeast(minimumLevel)) {
            history.add(event);
            LOGGER.info(event.toString());
        }
    }

    /**
     * Convenience method to build and send a notification in one call.
     */
    public void notify(String deploymentId, String environment,
                       String message, NotificationLevel level) {
        send(new NotificationEvent(deploymentId, environment, message, level));
    }

    /**
     * Returns an unmodifiable view of all stored notification events.
     */
    public List<NotificationEvent> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /**
     * Returns only events at or above the given level.
     */
    public List<NotificationEvent> getHistory(NotificationLevel atLeast) {
        List<NotificationEvent> filtered = new ArrayList<>();
        for (NotificationEvent e : history) {
            if (e.getLevel().isAtLeast(atLeast)) {
                filtered.add(e);
            }
        }
        return Collections.unmodifiableList(filtered);
    }

    public NotificationLevel getMinimumLevel() {
        return minimumLevel;
    }
}
