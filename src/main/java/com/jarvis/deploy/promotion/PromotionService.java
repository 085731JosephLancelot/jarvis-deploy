package com.jarvis.deploy.promotion;

import com.jarvis.deploy.audit.AuditEvent;
import com.jarvis.deploy.audit.AuditLogger;
import com.jarvis.deploy.deployment.Deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages environment-to-environment promotion of deployments based on registered policies.
 */
public class PromotionService {

    private final List<PromotionPolicy> policies = new ArrayList<>();
    private final AuditLogger auditLogger;

    public PromotionService(AuditLogger auditLogger) {
        if (auditLogger == null) throw new IllegalArgumentException("AuditLogger must not be null");
        this.auditLogger = auditLogger;
    }

    public void registerPolicy(PromotionPolicy policy) {
        if (policy == null) throw new IllegalArgumentException("Policy must not be null");
        boolean duplicate = policies.stream().anyMatch(p -> p.getId().equals(policy.getId()));
        if (duplicate) throw new IllegalStateException("Policy with id '" + policy.getId() + "' is already registered");
        policies.add(policy);
    }

    public Optional<PromotionPolicy> findPolicy(String sourceEnv, String targetEnv) {
        return policies.stream()
                .filter(p -> p.matches(sourceEnv, targetEnv))
                .findFirst();
    }

    public PromotionResult promote(Deployment deployment, String targetEnvironment, String initiatedBy) {
        if (deployment == null) throw new IllegalArgumentException("Deployment must not be null");
        if (targetEnvironment == null || targetEnvironment.isBlank()) throw new IllegalArgumentException("Target environment must not be blank");

        String sourceEnv = deployment.getEnvironment();
        Optional<PromotionPolicy> policyOpt = findPolicy(sourceEnv, targetEnvironment);

        if (policyOpt.isEmpty()) {
            String reason = String.format("No promotion policy found for %s -> %s", sourceEnv, targetEnvironment);
            auditLogger.log(new AuditEvent("PROMOTION_DENIED", initiatedBy,
                    "Deployment " + deployment.getId() + ": " + reason));
            return PromotionResult.denied(reason);
        }

        PromotionPolicy policy = policyOpt.get();

        if (policy.isRequiresApproval()) {
            auditLogger.log(new AuditEvent("PROMOTION_PENDING_APPROVAL", initiatedBy,
                    "Deployment " + deployment.getId() + " awaiting approval for promotion to " + targetEnvironment));
            return PromotionResult.pendingApproval("Approval required before promoting to " + targetEnvironment);
        }

        auditLogger.log(new AuditEvent("PROMOTION_SUCCESS", initiatedBy,
                "Deployment " + deployment.getId() + " promoted from " + sourceEnv + " to " + targetEnvironment));
        return PromotionResult.success(targetEnvironment);
    }

    public List<PromotionPolicy> getPolicies() {
        return List.copyOf(policies);
    }
}
