package com.jarvis.deploy.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationEventTest {

    @Test
    void constructor_setsAllFields() {
        NotificationEvent event = new NotificationEvent(
                "dep-001", "staging", "Deployment started", NotificationLevel.INFO);

        assertEquals("dep-001",  event.getDeploymentId());
        assertEquals("staging",  event.getEnvironment());
        assertEquals("Deployment started", event.getMessage());
        assertEquals(NotificationLevel.INFO, event.getLevel());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void constructor_throwsOnNullDeploymentId() {
        assertThrows(NullPointerException.class, () ->
                new NotificationEvent(null, "prod", "msg", NotificationLevel.ERROR));
    }

    @Test
    void constructor_throwsOnNullEnvironment() {
        assertThrows(NullPointerException.class, () ->
                new NotificationEvent("dep-002", null, "msg", NotificationLevel.WARNING));
    }

    @Test
    void constructor_throwsOnNullMessage() {
        assertThrows(NullPointerException.class, () ->
                new NotificationEvent("dep-003", "prod", null, NotificationLevel.INFO));
    }

    @Test
    void toString_containsKeyFields() {
        NotificationEvent event = new NotificationEvent(
                "dep-004", "production", "Rollback triggered", NotificationLevel.CRITICAL);
        String str = event.toString();
        assertTrue(str.contains("CRITICAL"));
        assertTrue(str.contains("production"));
        assertTrue(str.contains("dep-004"));
        assertTrue(str.contains("Rollback triggered"));
    }
}
