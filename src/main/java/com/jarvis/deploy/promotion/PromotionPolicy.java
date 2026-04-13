package com.jarvis.deploy.promotion;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines the rules governing promotion of a deployment from one environment to another.
 */
public class PromotionPolicy {

    private final String id;
    private final String sourceEnvironment;
    private final String targetEnvironment;
    private final boolean requiresApproval;
    private final boolean requiresHealthCheck;
    private final Set<String> requiredTags;

    public PromotionPolicy(String id,
                           String sourceEnvironment,
                           String targetEnvironment,
                           boolean requiresApproval,
                           boolean requiresHealthCheck,
                           Set<String> requiredTags) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Policy id must not be blank");
        if (sourceEnvironment == null || sourceEnvironment.isBlank()) throw new IllegalArgumentException("Source environment must not be blank");
        if (targetEnvironment == null || targetEnvironment.isBlank()) throw new IllegalArgumentException("Target environment must not be blank");
        if (sourceEnvironment.equals(targetEnvironment)) throw new IllegalArgumentException("Source and target environments must differ");
        this.id = id;
        this.sourceEnvironment = sourceEnvironment;
        this.targetEnvironment = targetEnvironment;
        this.requiresApproval = requiresApproval;
        this.requiresHealthCheck = requiresHealthCheck;
        this.requiredTags = requiredTags == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(requiredTags));
    }

    public String getId() { return id; }
    public String getSourceEnvironment() { return sourceEnvironment; }
    public String getTargetEnvironment() { return targetEnvironment; }
    public boolean isRequiresApproval() { return requiresApproval; }
    public boolean isRequiresHealthCheck() { return requiresHealthCheck; }
    public Set<String> getRequiredTags() { return requiredTags; }

    public boolean matches(String source, String target) {
        return this.sourceEnvironment.equalsIgnoreCase(source)
                && this.targetEnvironment.equalsIgnoreCase(target);
    }

    @Override
    public String toString() {
        return String.format("PromotionPolicy{id='%s', %s -> %s, approval=%b, healthCheck=%b, tags=%s}",
                id, sourceEnvironment, targetEnvironment, requiresApproval, requiresHealthCheck, requiredTags);
    }
}
