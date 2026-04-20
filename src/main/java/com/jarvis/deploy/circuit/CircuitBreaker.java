package com.jarvis.deploy.circuit;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit breaker that guards deployment operations from cascading failures.
 * Transitions: CLOSED -> OPEN (on threshold) -> HALF_OPEN (after cooldown) -> CLOSED/OPEN.
 */
public class CircuitBreaker {

    private final String name;
    private final int failureThreshold;
    private final long cooldownMillis;
    private final Clock clock;

    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicReference<CircuitState> state = new AtomicReference<>(CircuitState.CLOSED);
    private volatile Instant openedAt = null;

    public CircuitBreaker(String name, int failureThreshold, long cooldownMillis, Clock clock) {
        if (failureThreshold <= 0) throw new IllegalArgumentException("failureThreshold must be > 0");
        if (cooldownMillis <= 0) throw new IllegalArgumentException("cooldownMillis must be > 0");
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.cooldownMillis = cooldownMillis;
        this.clock = clock;
    }

    public CircuitBreaker(String name, int failureThreshold, long cooldownMillis) {
        this(name, failureThreshold, cooldownMillis, Clock.systemUTC());
    }

    /** Returns true if the circuit allows the request to proceed. */
    public boolean allowRequest() {
        CircuitState current = state.get();
        if (current == CircuitState.CLOSED) return true;
        if (current == CircuitState.OPEN) {
            if (cooldownElapsed()) {
                state.compareAndSet(CircuitState.OPEN, CircuitState.HALF_OPEN);
                return true;
            }
            return false;
        }
        // HALF_OPEN: allow exactly one probe
        return true;
    }

    /** Record a successful operation — resets failure count and closes the circuit. */
    public void recordSuccess() {
        failureCount.set(0);
        state.set(CircuitState.CLOSED);
        openedAt = null;
    }

    /** Record a failed operation — may trip the circuit open. */
    public void recordFailure() {
        int failures = failureCount.incrementAndGet();
        if (state.get() == CircuitState.HALF_OPEN || failures >= failureThreshold) {
            if (state.compareAndSet(state.get(), CircuitState.OPEN)) {
                openedAt = clock.instant();
            }
        }
    }

    public CircuitState getState() {
        return state.get();
    }

    public String getName() {
        return name;
    }

    public int getFailureCount() {
        return failureCount.get();
    }

    private boolean cooldownElapsed() {
        return openedAt != null &&
               clock.instant().toEpochMilli() - openedAt.toEpochMilli() >= cooldownMillis;
    }

    @Override
    public String toString() {
        return "CircuitBreaker{name='" + name + "', state=" + state.get() +
               ", failures=" + failureCount.get() + "}";
    }
}
