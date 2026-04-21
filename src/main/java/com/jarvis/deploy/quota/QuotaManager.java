package com.jarvis.deploy.quota;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages quota enforcement for deployments. Tracks usage per quota key
 * (derived from scope + identifier) and enforces configured {@link QuotaPolicy} rules.
 */
public class QuotaManager {

    private final Map<String, QuotaPolicy> policies = new ConcurrentHashMap<>();
    // key -> list of deployment timestamps within the current window
    private final Map<String, List<Instant>> usageLog = new ConcurrentHashMap<>();

    public void registerPolicy(QuotaPolicy policy) {
        Objects.requireNonNull(policy, "policy must not be null");
        policies.put(policy.getPolicyId(), policy);
    }

    public Optional<QuotaPolicy> getPolicy(String policyId) {
        return Optional.ofNullable(policies.get(policyId));
    }

    /**
     * Checks whether a deployment is allowed under the given policy for the given key.
     *
     * @param policyId   the policy to evaluate
     * @param scopeKey   the scoped identifier (e.g. environment name, user id)
     * @throws QuotaExceededException if the hard limit has been reached
     * @return true if allowed (or soft-limit warning), false if soft limit exceeded
     */
    public boolean checkQuota(String policyId, String scopeKey) {
        QuotaPolicy policy = policies.get(policyId);
        if (policy == null) throw new IllegalArgumentException("Unknown policy: " + policyId);

        String usageKey = policyId + ":" + scopeKey;
        Instant now = Instant.now();
        Instant windowStart = now.minus(policy.getWindow());

        List<Instant> timestamps = usageLog.computeIfAbsent(usageKey, k -> new ArrayList<>());
        // Evict timestamps outside the window
        timestamps.removeIf(t -> t.isBefore(windowStart));

        int current = timestamps.size();
        if (current >= policy.getMaxDeployments()) {
            if (policy.isHardLimit()) {
                throw new QuotaExceededException(
                        String.format("Quota exceeded for policy '%s' key '%s': %d/%d in window %s",
                                policyId, scopeKey, current, policy.getMaxDeployments(), policy.getWindow()));
            }
            return false; // soft limit
        }
        return true;
    }

    /**
     * Records a deployment event against the given policy and scope key.
     */
    public void recordUsage(String policyId, String scopeKey) {
        if (!policies.containsKey(policyId)) throw new IllegalArgumentException("Unknown policy: " + policyId);
        String usageKey = policyId + ":" + scopeKey;
        usageLog.computeIfAbsent(usageKey, k -> new ArrayList<>()).add(Instant.now());
    }

    public int currentUsage(String policyId, String scopeKey) {
        QuotaPolicy policy = policies.get(policyId);
        if (policy == null) return 0;
        String usageKey = policyId + ":" + scopeKey;
        Instant windowStart = Instant.now().minus(policy.getWindow());
        List<Instant> timestamps = usageLog.getOrDefault(usageKey, Collections.emptyList());
        return (int) timestamps.stream().filter(t -> !t.isBefore(windowStart)).count();
    }

    public void removePolicy(String policyId) {
        policies.remove(policyId);
    }

    public Set<String> listPolicyIds() {
        return Collections.unmodifiableSet(policies.keySet());
    }
}
