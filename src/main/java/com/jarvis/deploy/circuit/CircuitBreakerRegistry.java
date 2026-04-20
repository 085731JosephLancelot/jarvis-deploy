package com.jarvis.deploy.circuit;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for managing named {@link CircuitBreaker} instances.
 * Provides lookup, registration, and bulk-reset capabilities.
 */
public class CircuitBreakerRegistry {

    private final Map<String, CircuitBreaker> breakers = new ConcurrentHashMap<>();
    private final int defaultFailureThreshold;
    private final long defaultCooldownMillis;

    public CircuitBreakerRegistry(int defaultFailureThreshold, long defaultCooldownMillis) {
        this.defaultFailureThreshold = defaultFailureThreshold;
        this.defaultCooldownMillis = defaultCooldownMillis;
    }

    /**
     * Returns an existing breaker or creates one with default settings.
     */
    public CircuitBreaker getOrCreate(String name) {
        return breakers.computeIfAbsent(name,
                n -> new CircuitBreaker(n, defaultFailureThreshold, defaultCooldownMillis));
    }

    /**
     * Registers a pre-configured breaker. Overwrites any existing entry.
     */
    public void register(CircuitBreaker breaker) {
        breakers.put(breaker.getName(), breaker);
    }

    public Optional<CircuitBreaker> find(String name) {
        return Optional.ofNullable(breakers.get(name));
    }

    public boolean remove(String name) {
        return breakers.remove(name) != null;
    }

    /** Resets all breakers to CLOSED state. */
    public void resetAll() {
        breakers.values().forEach(CircuitBreaker::recordSuccess);
    }

    public Map<String, CircuitBreaker> all() {
        return Collections.unmodifiableMap(breakers);
    }

    public int size() {
        return breakers.size();
    }
}
