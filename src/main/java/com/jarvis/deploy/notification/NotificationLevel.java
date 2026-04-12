package com.jarvis.deploy.notification;

/**
 * Defines the severity/priority level for deployment notifications.
 */
public enum NotificationLevel {
    INFO,
    WARNING,
    ERROR,
    CRITICAL;

    public boolean isAtLeast(NotificationLevel other) {
        return this.ordinal() >= other.ordinal();
    }
}
