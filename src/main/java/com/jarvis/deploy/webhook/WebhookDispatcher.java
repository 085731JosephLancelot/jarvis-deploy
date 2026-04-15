package com.jarvis.deploy.webhook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Dispatches webhook events to registered listeners, optionally filtered by event type.
 */
public class WebhookDispatcher {

    private static final Logger LOGGER = Logger.getLogger(WebhookDispatcher.class.getName());

    private final Map<WebhookEventType, List<Consumer<WebhookEvent>>> listeners =
            new EnumMap<>(WebhookEventType.class);
    private final List<Consumer<WebhookEvent>> globalListeners = new ArrayList<>();

    /**
     * Registers a listener for a specific event type.
     */
    public void subscribe(WebhookEventType type, Consumer<WebhookEvent> listener) {
        if (type == null || listener == null) {
            throw new IllegalArgumentException("Event type and listener must not be null");
        }
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
        LOGGER.fine("Subscribed listener for event type: " + type);
    }

    /**
     * Registers a listener that receives all event types.
     */
    public void subscribeAll(Consumer<WebhookEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener must not be null");
        }
        globalListeners.add(listener);
        LOGGER.fine("Subscribed global listener");
    }

    /**
     * Dispatches a webhook event to all matching listeners.
     */
    public void dispatch(WebhookEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        LOGGER.info("Dispatching webhook event: " + event);

        List<Consumer<WebhookEvent>> typed = listeners.getOrDefault(event.getType(), Collections.emptyList());
        for (Consumer<WebhookEvent> listener : typed) {
            invokeListener(listener, event);
        }
        for (Consumer<WebhookEvent> listener : globalListeners) {
            invokeListener(listener, event);
        }
    }

    private void invokeListener(Consumer<WebhookEvent> listener, WebhookEvent event) {
        try {
            listener.accept(event);
        } catch (Exception ex) {
            LOGGER.warning("Webhook listener threw exception for event " + event.getId() + ": " + ex.getMessage());
        }
    }

    public int listenerCount(WebhookEventType type) {
        return listeners.getOrDefault(type, Collections.emptyList()).size();
    }

    public int globalListenerCount() {
        return globalListeners.size();
    }
}
