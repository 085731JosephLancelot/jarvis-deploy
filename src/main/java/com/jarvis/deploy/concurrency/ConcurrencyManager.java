package com.jarvis.deploy.concurrency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages deployment concurrency per environment, enforcing the configured
 * {@link ConcurrencyPolicy} and tracking active deployment slots.
 */
public class ConcurrencyManager {

    private final Map<String, ConcurrencyPolicy> policies = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> activeCount = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> queuedCount = new ConcurrentHashMap<>();

    public void registerPolicy(ConcurrencyPolicy policy) {
        policies.put(policy.getEnvironment(), policy);
        activeCount.putIfAbsent(policy.getEnvironment(), new AtomicInteger(0));
        queuedCount.putIfAbsent(policy.getEnvironment(), new AtomicInteger(0));
    }

    /**
     * Attempts to acquire a deployment slot for the given environment.
     *
     * @return AcquireResult indicating whether the slot was acquired, queued, or rejected.
     */
    public AcquireResult tryAcquire(String environment) {
        ConcurrencyPolicy policy = policies.get(environment);
        if (policy == null) {
            // No policy registered — allow by default
            activeCount.computeIfAbsent(environment, e -> new AtomicInteger(0)).incrementAndGet();
            return AcquireResult.ACQUIRED;
        }

        AtomicInteger active = activeCount.get(environment);
        int current = active.get();

        if (current < policy.getMaxConcurrent()) {
            if (active.compareAndSet(current, current + 1)) {
                return AcquireResult.ACQUIRED;
            }
            // Retry on CAS failure
            return tryAcquire(environment);
        }

        switch (policy.getOverflowStrategy()) {
            case REJECT:
                return AcquireResult.REJECTED;
            case QUEUE:
                AtomicInteger queued = queuedCount.get(environment);
                if (queued.get() < policy.getQueueCapacity()) {
                    queued.incrementAndGet();
                    return AcquireResult.QUEUED;
                }
                return AcquireResult.REJECTED;
            case CANCEL_OLDEST:
                // Caller is responsible for cancelling; slot is granted immediately
                return AcquireResult.CANCEL_OLDEST;
            default:
                return AcquireResult.REJECTED;
        }
    }

    public void release(String environment) {
        AtomicInteger active = activeCount.get(environment);
        if (active != null) {
            active.updateAndGet(v -> Math.max(0, v - 1));
        }
    }

    public void releaseQueued(String environment) {
        AtomicInteger queued = queuedCount.get(environment);
        if (queued != null) {
            queued.updateAndGet(v -> Math.max(0, v - 1));
        }
    }

    public int getActiveCount(String environment) {
        AtomicInteger counter = activeCount.get(environment);
        return counter == null ? 0 : counter.get();
    }

    public int getQueuedCount(String environment) {
        AtomicInteger counter = queuedCount.get(environment);
        return counter == null ? 0 : counter.get();
    }

    public boolean hasPolicy(String environment) {
        return policies.containsKey(environment);
    }

    public enum AcquireResult {
        ACQUIRED,
        QUEUED,
        REJECTED,
        CANCEL_OLDEST
    }
}
