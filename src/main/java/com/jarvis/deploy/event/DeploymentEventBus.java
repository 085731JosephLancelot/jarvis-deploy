package com.jarvis.deploy.event;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DeploymentEventBus {

    private final Map<DeploymentEventType, List<Consumer<DeploymentEvent>>> listeners = new EnumMap<>(DeploymentEventType.class);
    private final List<DeploymentEvent> eventHistory = new ArrayList<>();

    public void subscribe(DeploymentEventType type, Consumer<DeploymentEvent> listener) {
        if (type == null || listener == null) throw new IllegalArgumentException("Type and listener must not be null");
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
    }

    public void unsubscribe(DeploymentEventType type, Consumer<DeploymentEvent> listener) {
        List<Consumer<DeploymentEvent>> subs = listeners.get(type);
        if (subs != null) subs.remove(listener);
    }

    public void publish(DeploymentEvent event) {
        if (event == null) throw new IllegalArgumentException("Event must not be null");
        eventHistory.add(event);
        List<Consumer<DeploymentEvent>> subs = listeners.getOrDefault(event.getType(), List.of());
        for (Consumer<DeploymentEvent> listener : subs) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                // log and continue
            }
        }
    }

    public List<DeploymentEvent> getHistory() {
        return List.copyOf(eventHistory);
    }

    public List<DeploymentEvent> getHistoryByType(DeploymentEventType type) {
        return eventHistory.stream().filter(e -> e.getType() == type).toList();
    }

    public void clearHistory() {
        eventHistory.clear();
    }
}
