package com.jarvis.deploy.badge;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing deployment badges per service and environment.
 */
public class BadgeRegistry {

    private final Map<String, DeploymentBadge> badges = new ConcurrentHashMap<>();

    private String buildKey(String serviceId, String environment) {
        return serviceId + "::" + environment;
    }

    /**
     * Registers a new badge or replaces an existing one.
     */
    public void register(DeploymentBadge badge) {
        Objects.requireNonNull(badge, "badge must not be null");
        badges.put(buildKey(badge.getServiceId(), badge.getEnvironment()), badge);
    }

    /**
     * Updates the status and version of an existing badge.
     * If no badge exists for the given key, a new one is created.
     */
    public DeploymentBadge updateOrCreate(String serviceId, String environment, BadgeStatus status, String version) {
        String key = buildKey(serviceId, environment);
        DeploymentBadge badge = badges.computeIfAbsent(key,
                k -> new DeploymentBadge(serviceId, environment, version));
        badge.update(status, version);
        return badge;
    }

    /**
     * Retrieves a badge by service ID and environment.
     */
    public Optional<DeploymentBadge> get(String serviceId, String environment) {
        return Optional.ofNullable(badges.get(buildKey(serviceId, environment)));
    }

    /**
     * Returns all badges currently registered.
     */
    public List<DeploymentBadge> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(badges.values()));
    }

    /**
     * Returns all badges for a specific environment.
     */
    public List<DeploymentBadge> getByEnvironment(String environment) {
        List<DeploymentBadge> result = new ArrayList<>();
        for (DeploymentBadge badge : badges.values()) {
            if (badge.getEnvironment().equals(environment)) {
                result.add(badge);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Removes a badge from the registry.
     */
    public boolean remove(String serviceId, String environment) {
        return badges.remove(buildKey(serviceId, environment)) != null;
    }

    public int size() {
        return badges.size();
    }
}
