package com.jarvis.deploy.deployment;

import com.jarvis.deploy.audit.AuditEvent;
import com.jarvis.deploy.audit.AuditLogger;
import com.jarvis.deploy.environment.Environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Manages the lifecycle of deployments including creation, execution, and rollback.
 */
public class DeploymentService {

    private final AuditLogger auditLogger;
    private final List<Deployment> deploymentHistory = new ArrayList<>();

    public DeploymentService(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    public Deployment deploy(String artifactVersion, Environment environment) {
        Deployment deployment = new Deployment(artifactVersion, environment);
        deploymentHistory.add(deployment);

        auditLogger.log(new AuditEvent("DEPLOY_STARTED",
                String.format("Deploying version %s to %s [id=%s]",
                        artifactVersion, environment.getName(), deployment.getId())));

        deployment.setStatus(DeploymentStatus.IN_PROGRESS);

        try {
            environment.validate();
            // Simulate deployment logic
            deployment.setStatus(DeploymentStatus.SUCCESS);
            auditLogger.log(new AuditEvent("DEPLOY_SUCCESS",
                    String.format("Deployment %s succeeded", deployment.getId())));
        } catch (Exception e) {
            deployment.setStatus(DeploymentStatus.FAILED);
            auditLogger.log(new AuditEvent("DEPLOY_FAILED",
                    String.format("Deployment %s failed: %s", deployment.getId(), e.getMessage())));
        }

        return deployment;
    }

    public Optional<Deployment> rollback(String deploymentId) {
        return deploymentHistory.stream()
                .filter(d -> d.getId().equals(deploymentId) && d.getStatus().isRollbackable())
                .findFirst()
                .map(d -> {
                    d.setStatus(DeploymentStatus.ROLLED_BACK);
                    auditLogger.log(new AuditEvent("ROLLBACK",
                            String.format("Rolled back deployment %s (version %s on %s)",
                                    d.getId(), d.getArtifactVersion(), d.getEnvironment().getName())));
                    return d;
                });
    }

    public List<Deployment> getHistory() {
        return Collections.unmodifiableList(deploymentHistory);
    }

    public List<Deployment> getHistoryForEnvironment(Environment environment) {
        return deploymentHistory.stream()
                .filter(d -> d.getEnvironment().getName().equals(environment.getName()))
                .toList();
    }
}
