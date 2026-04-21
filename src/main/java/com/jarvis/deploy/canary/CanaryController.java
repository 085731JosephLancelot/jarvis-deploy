package com.jarvis.deploy.canary;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages active canary deployments: tracks current traffic weights,
 * advances steps, and decides whether to promote or abort.
 */
public class CanaryController {

    public enum CanaryState { RUNNING, PROMOTED, ABORTED }

    private static class CanaryEntry {
        final CanaryPolicy policy;
        int currentPercent;
        CanaryState state;

        CanaryEntry(CanaryPolicy policy) {
            this.policy = policy;
            this.currentPercent = policy.getInitialTrafficPercent();
            this.state = CanaryState.RUNNING;
        }
    }

    private final Map<String, CanaryEntry> active = new ConcurrentHashMap<>();

    /**
     * Registers a new canary deployment under the given deploymentId.
     */
    public void start(String deploymentId, CanaryPolicy policy) {
        Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        Objects.requireNonNull(policy, "policy must not be null");
        if (active.containsKey(deploymentId)) {
            throw new IllegalStateException("Canary already active for deployment: " + deploymentId);
        }
        active.put(deploymentId, new CanaryEntry(policy));
    }

    /**
     * Returns the current traffic percentage for the canary build.
     */
    public int getCurrentTrafficPercent(String deploymentId) {
        return getEntry(deploymentId).currentPercent;
    }

    /**
     * Advances the canary one step if the error rate is acceptable.
     * Automatically promotes when the target traffic percent is reached.
     *
     * @return the new state after the step
     */
    public CanaryState step(String deploymentId, double currentErrorRate) {
        CanaryEntry entry = getEntry(deploymentId);
        if (entry.state != CanaryState.RUNNING) {
            return entry.state;
        }
        if (!entry.policy.isErrorRateAcceptable(currentErrorRate)) {
            entry.state = CanaryState.ABORTED;
            active.remove(deploymentId);
            return CanaryState.ABORTED;
        }
        int next = Math.min(entry.currentPercent + entry.policy.getStepPercent(),
                entry.policy.getTargetTrafficPercent());
        entry.currentPercent = next;
        if (next >= entry.policy.getTargetTrafficPercent()) {
            entry.state = CanaryState.PROMOTED;
            active.remove(deploymentId);
        }
        return entry.state;
    }

    /**
     * Immediately aborts the canary deployment.
     */
    public void abort(String deploymentId) {
        CanaryEntry entry = getEntry(deploymentId);
        entry.state = CanaryState.ABORTED;
        active.remove(deploymentId);
    }

    public CanaryState getState(String deploymentId) {
        return getEntry(deploymentId).state;
    }

    public boolean isActive(String deploymentId) {
        return active.containsKey(deploymentId);
    }

    private CanaryEntry getEntry(String deploymentId) {
        CanaryEntry entry = active.get(deploymentId);
        if (entry == null) {
            throw new IllegalArgumentException("No active canary for deployment: " + deploymentId);
        }
        return entry;
    }
}
