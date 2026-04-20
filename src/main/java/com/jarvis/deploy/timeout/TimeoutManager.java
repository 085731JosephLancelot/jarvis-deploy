package com.jarvis.deploy.timeout;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages deployment timeouts across active deployments.
 */
public class TimeoutManager {

    private final Map<String, DeploymentTimeout> timeouts = new ConcurrentHashMap<>();
    private final Duration defaultTimeout;

    public TimeoutManager(Duration defaultTimeout) {
        if (defaultTimeout == null || defaultTimeout.isNegative() || defaultTimeout.isZero()) {
            throw new IllegalArgumentException("Default timeout must be a positive duration");
        }
        this.defaultTimeout = defaultTimeout;
    }

    public DeploymentTimeout register(String deploymentId) {
        return register(deploymentId, defaultTimeout);
    }

    public DeploymentTimeout register(String deploymentId, Duration limit) {
        DeploymentTimeout timeout = new DeploymentTimeout(deploymentId, limit);
        timeouts.put(deploymentId, timeout);
        return timeout;
    }

    public Optional<DeploymentTimeout> get(String deploymentId) {
        return Optional.ofNullable(timeouts.get(deploymentId));
    }

    public boolean isExpired(String deploymentId) {
        return get(deploymentId).map(DeploymentTimeout::isExpired).orElse(false);
    }

    public void cancel(String deploymentId) {
        get(deploymentId).ifPresent(DeploymentTimeout::cancel);
    }

    public void remove(String deploymentId) {
        timeouts.remove(deploymentId);
    }

    public List<DeploymentTimeout> getExpired() {
        return timeouts.values().stream()
                .filter(DeploymentTimeout::isExpired)
                .collect(Collectors.toList());
    }

    public Collection<DeploymentTimeout> getAll() {
        return Collections.unmodifiableCollection(timeouts.values());
    }

    public int size() {
        return timeouts.size();
    }

    public Duration getDefaultTimeout() {
        return defaultTimeout;
    }
}
