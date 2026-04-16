package com.jarvis.deploy.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentEventBusTest {

    private DeploymentEventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new DeploymentEventBus();
    }

    @Test
    void testSubscribeAndPublish() {
        List<DeploymentEvent> received = new ArrayList<>();
        eventBus.subscribe(DeploymentEventType.DEPLOYMENT_STARTED, received::add);
        DeploymentEvent event = new DeploymentEvent(DeploymentEventType.DEPLOYMENT_STARTED, "d1", "staging");
        eventBus.publish(event);
        assertEquals(1, received.size());
        assertEquals(event, received.get(0));
    }

    @Test
    void testPublishToWrongTypeNotReceived() {
        List<DeploymentEvent> received = new ArrayList<>();
        eventBus.subscribe(DeploymentEventType.DEPLOYMENT_COMPLETED, received::add);
        eventBus.publish(new DeploymentEvent(DeploymentEventType.DEPLOYMENT_STARTED, "d1", "prod"));
        assertTrue(received.isEmpty());
    }

    @Test
    void testUnsubscribe() {
        List<DeploymentEvent> received = new ArrayList<>();
        var listener = (java.util.function.Consumer<DeploymentEvent>) received::add;
        eventBus.subscribe(DeploymentEventType.DEPLOYMENT_FAILED, listener);
        eventBus.unsubscribe(DeploymentEventType.DEPLOYMENT_FAILED, listener);
        eventBus.publish(new DeploymentEvent(DeploymentEventType.DEPLOYMENT_FAILED, "d2", "dev"));
        assertTrue(received.isEmpty());
    }

    @Test
    void testHistoryTracking() {
        eventBus.publish(new DeploymentEvent(DeploymentEventType.DEPLOYMENT_STARTED, "d1", "staging"));
        eventBus.publish(new DeploymentEvent(DeploymentEventType.DEPLOYMENT_COMPLETED, "d1", "staging"));
        assertEquals(2, eventBus.getHistory().size());
    }

    @Test
    void testGetHistoryByType() {
        eventBus.publish(new DeploymentEvent(DeploymentEventType.DEPLOYMENT_STARTED, "d1", "staging"));
        eventBus.publish(new DeploymentEvent(DeploymentEventType.DEPLOYMENT_FAILED, "d2", "prod"));
        eventBus.publish(new DeploymentEvent(DeploymentEventType.DEPLOYMENT_STARTED, "d3", "dev"));
        List<DeploymentEvent> started = eventBus.getHistoryByType(DeploymentEventType.DEPLOYMENT_STARTED);
        assertEquals(2, started.size());
    }

    @Test
    void testClearHistory() {
        eventBus.publish(new DeploymentEvent(DeploymentEventType.ROLLBACK_STARTED, "d1", "prod"));
        eventBus.clearHistory();
        assertTrue(eventBus.getHistory().isEmpty());
    }

    @Test
    void testPublishNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> eventBus.publish(null));
    }

    @Test
    void testSubscribeNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> eventBus.subscribe(null, e -> {}));
    }
}
