package com.jarvis.deploy.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WebhookDispatcherTest {

    private WebhookDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new WebhookDispatcher();
    }

    private WebhookEvent buildEvent(WebhookEventType type) {
        Map<String, String> payload = new HashMap<>();
        payload.put("version", "1.2.3");
        return new WebhookEvent(type, "deploy-001", "production", payload);
    }

    @Test
    void testSubscribeAndDispatchTypedListener() {
        List<WebhookEvent> received = new ArrayList<>();
        dispatcher.subscribe(WebhookEventType.DEPLOYMENT_STARTED, received::add);

        WebhookEvent event = buildEvent(WebhookEventType.DEPLOYMENT_STARTED);
        dispatcher.dispatch(event);

        assertEquals(1, received.size());
        assertEquals(WebhookEventType.DEPLOYMENT_STARTED, received.get(0).getType());
    }

    @Test
    void testTypedListenerDoesNotReceiveOtherEvents() {
        List<WebhookEvent> received = new ArrayList<>();
        dispatcher.subscribe(WebhookEventType.DEPLOYMENT_SUCCEEDED, received::add);

        dispatcher.dispatch(buildEvent(WebhookEventType.DEPLOYMENT_FAILED));

        assertTrue(received.isEmpty());
    }

    @Test
    void testGlobalListenerReceivesAllEvents() {
        List<WebhookEvent> received = new ArrayList<>();
        dispatcher.subscribeAll(received::add);

        dispatcher.dispatch(buildEvent(WebhookEventType.DEPLOYMENT_STARTED));
        dispatcher.dispatch(buildEvent(WebhookEventType.ROLLBACK_INITIATED));
        dispatcher.dispatch(buildEvent(WebhookEventType.HEALTH_CHECK_FAILED));

        assertEquals(3, received.size());
    }

    @Test
    void testFaultyListenerDoesNotPreventOthers() {
        List<WebhookEvent> received = new ArrayList<>();
        dispatcher.subscribe(WebhookEventType.DEPLOYMENT_STARTED, e -> { throw new RuntimeException("boom"); });
        dispatcher.subscribe(WebhookEventType.DEPLOYMENT_STARTED, received::add);

        assertDoesNotThrow(() -> dispatcher.dispatch(buildEvent(WebhookEventType.DEPLOYMENT_STARTED)));
        assertEquals(1, received.size());
    }

    @Test
    void testNullEventThrows() {
        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(null));
    }

    @Test
    void testNullListenerThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> dispatcher.subscribe(WebhookEventType.DEPLOYMENT_STARTED, null));
    }

    @Test
    void testListenerCount() {
        dispatcher.subscribe(WebhookEventType.APPROVAL_REQUESTED, e -> {});
        dispatcher.subscribe(WebhookEventType.APPROVAL_REQUESTED, e -> {});
        dispatcher.subscribeAll(e -> {});

        assertEquals(2, dispatcher.listenerCount(WebhookEventType.APPROVAL_REQUESTED));
        assertEquals(1, dispatcher.globalListenerCount());
    }

    @Test
    void testWebhookEventFields() {
        Map<String, String> payload = Map.of("service", "auth-service");
        WebhookEvent event = new WebhookEvent(WebhookEventType.ROLLBACK_COMPLETED, "dep-42", "staging", payload);

        assertNotNull(event.getId());
        assertEquals(WebhookEventType.ROLLBACK_COMPLETED, event.getType());
        assertEquals("dep-42", event.getDeploymentId());
        assertEquals("staging", event.getEnvironment());
        assertNotNull(event.getOccurredAt());
        assertEquals("auth-service", event.getPayload().get("service"));
    }
}
