package com.jarvis.deploy.circuit;

/**
 * Represents the possible states of a circuit breaker.
 */
public enum CircuitState {
    /** Circuit is closed — requests flow normally. */
    CLOSED,

    /** Circuit is open — requests are blocked to protect downstream. */
    OPEN,

    /** Circuit is half-open — a probe request is allowed to test recovery. */
    HALF_OPEN
}
