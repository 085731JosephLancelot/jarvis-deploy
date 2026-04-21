package com.jarvis.deploy.drain;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages connection-drain operations across environments before deployments.
 * Tracks drain state per environment and enforces DrainPolicy rules.
 */
public class DrainManager {

    public enum DrainState { IDLE, DRAINING, DRAINED, FORCED }

    private final Map<String, DrainPolicy> policies = new ConcurrentHashMap<>();
    private final Map<String, DrainState> states = new ConcurrentHashMap<>();
    private final Map<String, Instant> drainStartTimes = new ConcurrentHashMap<>();

    public void registerPolicy(DrainPolicy policy) {
        if (policy == null) throw new IllegalArgumentException("Policy must not be null");
        policies.put(policy.getEnvironmentName(), policy);
        states.put(policy.getEnvironmentName(), DrainState.IDLE);
    }

    public void beginDrain(String environmentName) {
        DrainPolicy policy = requirePolicy(environmentName);
        DrainState current = states.get(environmentName);
        if (current == DrainState.DRAINING) {
            throw new IllegalStateException("Drain already in progress for: " + environmentName);
        }
        states.put(environmentName, DrainState.DRAINING);
        drainStartTimes.put(environmentName, Instant.now());
    }

    public void completeDrain(String environmentName) {
        requirePolicy(environmentName);
        if (states.get(environmentName) != DrainState.DRAINING) {
            throw new IllegalStateException("No active drain for: " + environmentName);
        }
        states.put(environmentName, DrainState.DRAINED);
    }

    public boolean checkTimeout(String environmentName) {
        DrainPolicy policy = requirePolicy(environmentName);
        Instant start = drainStartTimes.get(environmentName);
        if (start == null) return false;
        boolean timedOut = Instant.now().isAfter(start.plus(policy.getTimeout()));
        if (timedOut && policy.isForceOnTimeout()) {
            states.put(environmentName, DrainState.FORCED);
        }
        return timedOut;
    }

    public void reset(String environmentName) {
        requirePolicy(environmentName);
        states.put(environmentName, DrainState.IDLE);
        drainStartTimes.remove(environmentName);
    }

    public DrainState getState(String environmentName) {
        return Optional.ofNullable(states.get(environmentName))
                .orElseThrow(() -> new IllegalArgumentException("No policy registered for: " + environmentName));
    }

    public boolean isReadyToDeploy(String environmentName) {
        DrainState state = getState(environmentName);
        return state == DrainState.DRAINED || state == DrainState.FORCED;
    }

    private DrainPolicy requirePolicy(String environmentName) {
        DrainPolicy policy = policies.get(environmentName);
        if (policy == null) {
            throw new IllegalArgumentException("No drain policy registered for: " + environmentName);
        }
        return policy;
    }
}
