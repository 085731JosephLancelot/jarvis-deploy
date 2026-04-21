package com.jarvis.deploy.quota;

import java.time.Duration;
import java.util.Objects;

/**
 * Defines the rules for a deployment quota: how many deployments are allowed
 * within a rolling time window for a given scope.
 */
public class QuotaPolicy {

    private final String policyId;
    private final QuotaScope scope;
    private final int maxDeployments;
    private final Duration window;
    private final boolean hardLimit;

    public QuotaPolicy(String policyId, QuotaScope scope, int maxDeployments, Duration window, boolean hardLimit) {
        if (maxDeployments <= 0) throw new IllegalArgumentException("maxDeployments must be positive");
        Objects.requireNonNull(policyId, "policyId must not be null");
        Objects.requireNonNull(scope, "scope must not be null");
        Objects.requireNonNull(window, "window must not be null");
        this.policyId = policyId;
        this.scope = scope;
        this.maxDeployments = maxDeployments;
        this.window = window;
        this.hardLimit = hardLimit;
    }

    public String getPolicyId() { return policyId; }
    public QuotaScope getScope() { return scope; }
    public int getMaxDeployments() { return maxDeployments; }
    public Duration getWindow() { return window; }
    public boolean isHardLimit() { return hardLimit; }

    @Override
    public String toString() {
        return String.format("QuotaPolicy{id='%s', scope=%s, max=%d, window=%s, hard=%b}",
                policyId, scope, maxDeployments, window, hardLimit);
    }
}
