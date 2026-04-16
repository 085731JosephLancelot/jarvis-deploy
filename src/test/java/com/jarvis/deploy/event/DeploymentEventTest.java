package com.jarvis.deploy.event;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentEventTest {

    @Test
    void testEventCreationDefaults() {
        DeploymentEvent event = new DeploymentEvent(DeploymentEventType.DEPLOYMENT_STARTED, "dep-1", "staging");
        assertNotNull(event.getEventId());
        assertEquals(DeploymentEventType.DEPLOYMENT_STARTED, event.getType());
        assertEquals("dep-1", event.getDeploymentId());
        assertEquals("staging", event.getEnvironment());
        assertNotNull(event.getOccurredAt());
        assertTrue(event.getMetadata().isEmpty());
    }

    @Test
    void testEventWithMetadata() {
        Map<String, String> meta = Map.of("version", "1.2.3", "user", "alice");
        DeploymentEvent event = new DeploymentEvent(DeploymentEventType.DEPLOYMENT_COMPLETED, "dep-2", "prod", meta);
        assertEquals("1.2.3", event.getMetadata().get("version"));
        assertEquals("alice", event.getMetadata().get("user"));
    }

    @Test
    void testMetadataIsImmutable() {
        DeploymentEvent event = new DeploymentEvent(DeploymentEventType.DEPLOYMENT_FAILED, "dep-3", "dev", Map.of("k", "v"));
        assertThrows(UnsupportedOperationException.class, () -> event.getMetadata().put("x", "y"));
    }

    @Test
    void testUniqueEventIds() {
        DeploymentEvent e1 = new DeploymentEvent(DeploymentEventType.ROLLBACK_STARTED, "dep-4", "prod");
        DeploymentEvent e2 = new DeploymentEvent(DeploymentEventType.ROLLBACK_STARTED, "dep-4", "prod");
        assertNotEquals(e1.getEventId(), e2.getEventId());
    }

    @Test
    void testToString() {
        DeploymentEvent event = new DeploymentEvent(DeploymentEventType.APPROVAL_REQUESTED, "dep-5", "staging");
        String str = event.toString();
        assertTrue(str.contains("APPROVAL_REQUESTED"));
        assertTrue(str.contains("dep-5"));
    }
}
