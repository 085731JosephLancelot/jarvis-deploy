package com.jarvis.deploy.throttle;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks and enforces deployment throttle policies per environment.
 */
public class DeploymentThrottler {

    private final Map<String, ThrottlePolicy> policies = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> windowRecords = new ConcurrentHashMap<>();

    public void registerPolicy(ThrottlePolicy policy) {
        Objects.requireNonNull(policy, "policy must not be null");
        policies.put(policy.getEnvironment(), policy);
        windowRecords.putIfAbsent(policy.getEnvironment(), new ArrayDeque<>());
    }

    public boolean isThrottled(String environment) {
        ThrottlePolicy policy = policies.get(environment);
        if (policy == null) {
            return false;
        }
        Deque<Instant> records = windowRecords.computeIfAbsent(environment, e -> new ArrayDeque<>());
        Instant cutoff = Instant.now().minus(policy.getWindowDuration());
        synchronized (records) {
            records.removeIf(ts -> ts.isBefore(cutoff));
            return records.size() >= policy.getMaxDeploymentsPerWindow();
        }
    }

    public ThrottleResult tryAcquire(String environment) {
        ThrottlePolicy policy = policies.get(environment);
        if (policy == null) {
            return ThrottleResult.allowed("No throttle policy configured for: " + environment);
        }
        if (isThrottled(environment)) {
            String reason = String.format("Throttle limit of %d per %s exceeded for environment '%s'",
                    policy.getMaxDeploymentsPerWindow(), policy.getWindowDuration(), environment);
            return policy.isHardLimit()
                    ? ThrottleResult.denied(reason)
                    : ThrottleResult.warned(reason);
        }
        Deque<Instant> records = windowRecords.get(environment);
        synchronized (records) {
            records.addLast(Instant.now());
        }
        return ThrottleResult.allowed("Deployment allowed for environment: " + environment);
    }

    public int getDeploymentCount(String environment) {
        ThrottlePolicy policy = policies.get(environment);
        if (policy == null) return 0;
        Deque<Instant> records = windowRecords.getOrDefault(environment, new ArrayDeque<>());
        Instant cutoff = Instant.now().minus(policy.getWindowDuration());
        synchronized (records) {
            records.removeIf(ts -> ts.isBefore(cutoff));
            return records.size();
        }
    }

    public void removePolicy(String environment) {
        policies.remove(environment);
        windowRecords.remove(environment);
    }

    public boolean hasPolicy(String environment) {
        return policies.containsKey(environment);
    }
}
